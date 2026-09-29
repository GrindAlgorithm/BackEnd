"""
시즌 3 시드 생성기 — ICPC Rocky Mountain Regional 2021 공개 패키지 → season3-seed.sql

사용:
    git clone --depth 1 https://github.com/icpc/na-rocky-mountain-2021-public rm2021
    python tools/gen_season3_seed.py rm2021/problems > src/main/resources/db/season3-seed.sql

선정 기준 (채점기 제약):
  - 정확 일치 채점만 지원 → float_tolerance·custom validator·"답이 여러 개" 문제 제외
  - problem_testcase.input/output 이 TEXT(64KB) → 64KB 넘는 케이스 제외
  - judge0 워커 1개·케이스마다 별도 실행 → 문제당 케이스 수 상한(MAX_CASES)
"""
import os
import re
import sys

# (short-name, 표시 번호, 티어, 레벨, 시간 제한 초, 메모리 MB, 태그들) — 티어·태그는 유형 기반 추정.
# 시간 제한 = 개발계 judge0(1GB VM)에서 잰 가장 느린 공식 풀이(C++/Java/Python) × 약 1.25, 최소 2초.
#   Java 가 이 VM 에서 특히 느리다(protectthepollen: C++ 0.32s / Java 8.22s) — 서버 증설 시 재측정 권장
PROBLEMS = [
    ("socialdistancing",  "A", "SILVER",   "IV",  2, 256, ["그리디", "구현"]),
    ("electionparadox",   "B", "SILVER",   "III", 2, 256, ["그리디", "정렬"]),
    ("rsamistake",        "C", "SILVER",   "I",   3, 256, ["수학", "정수론"]),
    ("wordlewithfriends", "D", "SILVER",   "I",   3, 256, ["구현", "문자열"]),
    ("slidecount",        "E", "GOLD",     "IV",  3, 256, ["투 포인터", "누적 합"]),
    ("snowballfight",     "F", "GOLD",     "III", 2, 256, ["수학", "시뮬레이션"]),
    ("protectthepollen",  "G", "GOLD",     "I",  10, 256, ["트리 DP", "배낭"]),
    ("antialiasing",      "H", "PLATINUM", "IV",  8, 256, ["기하", "분수"]),
]

SEASON_ID = 3
MAX_CASES = 12          # 공개 예제 포함 문제당 채점 케이스 상한
MAX_BYTES = 65000       # TEXT 컬럼 한도(65535) 여유


# ── LaTeX 지문 → plain text ──────────────────────────────────────
def latex_to_text(tex):
    t = re.sub(r"(?m)^%.*$", "", tex)                       # 주석 줄
    t = re.sub(r"\\problemname\{[^}]*\}", "", t)
    t = re.sub(r"\\begin\{figure\}.*?\\end\{figure\}", "", t, flags=re.S)
    t = re.sub(r"\\begin\{center\}.*?\\end\{center\}", "", t, flags=re.S)
    t = re.sub(r"\\footnote\{(?:[^{}]|\{[^{}]*\})*\}", "", t)
    # 표시 수식 \[ ... \] → 독립 줄
    t = re.sub(r"\\\[(.*?)\\\]", lambda m: "\n\n    " + m.group(1).strip() + "\n\n", t, flags=re.S)
    # 목록 → 들여쓴 글머리표 (중첩 깊이만큼 들여쓰기)
    out, depth = [], 0
    for line in t.split("\n"):
        s = line.strip()
        if re.match(r"\\begin\{(itemize|enumerate)\}", s):
            depth += 1
            out.append("")
            continue
        if re.match(r"\\end\{(itemize|enumerate)\}", s):
            depth -= 1
            out.append("")
            continue
        if s.startswith("\\item"):
            out.append("\x00" + "  " * (depth - 1) + "- " + s[len("\\item"):].strip())
            continue
        out.append(line)
    t = "\n".join(out)

    reps = [
        (r"\\leq?\b", "<="), (r"\\geq?\b", ">="), (r"\\neq\b", "!="), (r"\\cdot\b", "*"),
        (r"\\(min|max|log|gcd)\b", r"\1"),
        (r"\\ldots\b", "..."), (r"\\cdots\b", "..."), (r"\\dots\b", "..."),
        (r"\\,", ""), (r"~", " "), (r"\\#", "#"), (r"\\%", "%"),
    ]
    for a, b in reps:
        t = re.sub(a, b, t)
    for cmd in ("texttt", "textit", "textbf", "emph", "href", "url"):
        t = re.sub(r"\\" + cmd + r"\{([^{}]*)\}(\{[^{}]*\})?",
                   lambda m: (m.group(2)[1:-1] if cmd == "href" and m.group(2) else m.group(1)), t)
    t = re.sub(r"\{\\(em|bf|it)\s+([^{}]*)\}", r"\2", t)
    t = re.sub(r"\^\{([^{}]*)\}", r"^\1", t)                # 10^{12} → 10^12
    t = re.sub(r"_\{([^{}]+)\}", lambda m: "_" + (m.group(1) if re.fullmatch(r"\w+", m.group(1))
                                                     else "(" + m.group(1) + ")"), t)
    t = t.replace("$", "")
    t = re.sub(r"\\[a-zA-Z]+\*?", "", t)                    # 남은 명령어 제거
    t = t.replace("{", "").replace("}", "")

    # 줄 정리: 문단 안의 LaTeX 줄바꿈은 합치고, 문단·글머리표·수식 줄은 유지
    paras, cur = [], []
    in_item = False  # 직전 줄이 글머리표면, 이어지는 줄은 그 항목에 붙인다
    for line in t.split("\n"):
        raw = line.rstrip()
        s = re.sub(r"\s+", " ", raw).strip()
        if raw.startswith("\x00") or (raw.startswith("    ") and not in_item):
            if cur:
                paras.append(" ".join(cur)); cur = []
            paras.append(raw.replace("\x00", "").rstrip())
            in_item = raw.startswith("\x00")
            continue
        if not s:
            if cur:
                paras.append(" ".join(cur)); cur = []
            paras.append("")
            in_item = False
            continue
        if in_item:
            paras[-1] += " " + s
            continue
        cur.append(s)
    if cur:
        paras.append(" ".join(cur))
    text = "\n".join(paras)
    text = re.sub(r"\n{3,}", "\n\n", text).strip()
    text = re.sub(r"(?m)^(\s*- .*)\n\n(?=\s*- )", r"\1\n", text)   # 글머리표 사이 빈 줄 제거
    return text


def statement(problem_dir):
    tex = open(os.path.join(problem_dir, "problem_statement", "problem.tex"), encoding="utf-8").read()
    parts = re.split(r"\\section\*\{(Input|Output)\}", tex)
    desc, inp, outp = parts[0], "", ""
    for i in range(1, len(parts), 2):
        if parts[i] == "Input":
            inp = parts[i + 1]
        else:
            outp = parts[i + 1]
    return latex_to_text(desc), latex_to_text(inp), latex_to_text(outp)


# ── 테스트 데이터 ────────────────────────────────────────────────
def read(path):
    with open(path, encoding="utf-8", newline="") as f:
        return f.read()


def cases(problem_dir, kind):
    d = os.path.join(problem_dir, "data", kind)
    names = sorted(f[:-3] for f in os.listdir(d) if f.endswith(".in"))
    result = []
    for n in names:
        i, a = os.path.join(d, n + ".in"), os.path.join(d, n + ".ans")
        if os.path.exists(a):
            result.append((n, read(i), read(a)))
    return result


def pick_secret(secret, limit):
    """64KB 이하만, 큰 케이스(시간복잡도 검증) 절반 + 나머지는 고르게 섞어 limit 개."""
    fit = [c for c in secret if len(c[1].encode()) <= MAX_BYTES and len(c[2].encode()) <= MAX_BYTES]
    if len(fit) <= limit:
        return fit
    by_size = sorted(fit, key=lambda c: len(c[1]), reverse=True)
    big = by_size[: limit // 2]
    rest = [c for c in fit if c not in big]
    step = len(rest) / (limit - len(big))
    spread = [rest[int(k * step)] for k in range(limit - len(big))]
    chosen = {c[0] for c in big + spread}
    return [c for c in fit if c[0] in chosen]                  # 원래 순서 유지


def sql(s):
    return "'" + s.replace("\\", "\\\\").replace("'", "''") + "'"


def main(root):
    # Windows 기본 표준출력은 \n → \r\n 변환을 해 SQL 문자열(채점 입출력)에 CR 이 섞인다 — LF 고정
    sys.stdout.reconfigure(encoding="utf-8", newline="\n")
    w = sys.stdout.write
    w("-- 시즌 3 시드 — 시즌·문제·태그·본문·예제·채점 케이스·리워드 (자동 생성: tools/gen_season3_seed.py)\n")
    w("-- 출처: ICPC North America Rocky Mountain Regional 2021 (license: CC BY-SA)\n")
    w("--   https://github.com/icpc/na-rocky-mountain-2021-public\n")
    w("-- 본문은 원문 LaTeX 를 plain text 로 변환한 것(영문 원문 유지). 티어·태그·제한 시간은 유형 기반 추정.\n")
    w("-- 채점 케이스: 공개 예제(hidden=0) + 공식 비공개 데이터 중 64KB 이하(hidden=1), 문제당 최대 %d개.\n" % MAX_CASES)
    w("-- 시즌은 UPCOMING 으로 넣는다 — 시작일(10/1 KST)에 SeasonLifecycleService 가 CURRENT 로 전환하고,\n")
    w("-- 그 전까지는 공개 API 에서 시즌·문제가 모두 숨겨진다. ⚠ UPCOMING 을 아는 백엔드가 배포된 뒤에 적용할 것.\n\n")

    w("INSERT INTO season (id, name, start_date, end_date, status) VALUES\n")
    w("    (%d, 'Season 3', '2026-10-01', '2026-12-31', 'UPCOMING');\n\n" % SEASON_ID)

    w("INSERT INTO problem (problem_id, display_no, title, tier_name, tier_level, season_id,\n")
    w("                     time_limit_sec, memory_limit_mb, expected_complexity,\n")
    w("                     submission_count, accepted_count, solver_count, discussion_count) VALUES\n")
    rows = []
    for pid, no, tier, lvl, tl, mem, _ in PROBLEMS:
        name = re.search(r"\\problemname\{([^}]*)\}",
                         read(os.path.join(root, pid, "problem_statement", "problem.tex"))).group(1)
        rows.append("    (%s, %s, %s, %s, %s, %d, %d, %d, NULL, 0, 0, 0, 0)"
                    % (sql(pid), sql(no), sql(name), sql(tier), sql(lvl), SEASON_ID, tl, mem))
    w(",\n".join(rows) + ";\n\n")

    for pid, *_rest, tags in PROBLEMS:
        for tag in tags:
            w("INSERT INTO problem_tag (problem_id, tag) SELECT id, %s FROM problem WHERE problem_id = %s;\n"
              % (sql(tag), sql(pid)))
    w("\n")

    stats = []
    for pid, *_ in PROBLEMS:
        pdir = os.path.join(root, pid)
        desc, inp, outp = statement(pdir)
        w("-- ── %s ──\n" % pid)
        w("INSERT INTO problem_body (problem_id, description, input_spec, output_spec)\nSELECT id, %s, %s, %s "
          "FROM problem WHERE problem_id = %s;\n" % (sql(desc), sql(inp), sql(outp), sql(pid)))
        samples = cases(pdir, "sample")
        for k, (_, i, a) in enumerate(samples, 1):
            w("INSERT INTO problem_sample (problem_id, ordinal, input, output) SELECT id, %d, %s, %s "
              "FROM problem WHERE problem_id = %s;\n" % (k, sql(i), sql(a), sql(pid)))
        secret = pick_secret(cases(pdir, "secret"), max(0, MAX_CASES - len(samples)))
        ordinal = 0
        for hidden, group in ((0, samples), (1, secret)):
            for _, i, a in group:
                ordinal += 1
                w("INSERT INTO problem_testcase (problem_id, ordinal, input, output, hidden) SELECT id, %d, %s, %s, %d "
                  "FROM problem WHERE problem_id = %s;\n" % (ordinal, sql(i), sql(a), hidden, sql(pid)))
        w("\n")
        stats.append("%s: 예제 %d + 비공개 %d" % (pid, len(samples), len(secret)))

    w("INSERT INTO season_reward (id, season_id, name, color_key, condition_text, condition_type, threshold, sort_order) VALUES\n")
    w("    ('s3_champion', 3, 'S3 챔피언',      'gold',     '시즌 종료 시 1위',          'CHAMPION',      NULL, 1),\n")
    w("    ('s3_diamond',  3, 'S3 다이아',      'diamond',  '시즌 다이아 티어 도달',      'REACH_DIAMOND', NULL, 2),\n")
    w("    ('s3_clear',    3, 'S3 시즌 클리어', 'platinum', '시즌 문제 %d개 모두 클리어',  'CLEAR_ALL',     NULL, 3),\n" % len(PROBLEMS))
    w("    ('s3_first',    3, 'S3 첫 발걸음',   'silver',   '시즌 문제 1개 클리어',       'CLEAR_COUNT',   1,    4),\n")
    w("    ('s3_100',      3, 'S3 100문제',     'bronze',   '시즌 중 100문제 풀이',       'SOLVE_COUNT',   100,  5);\n")
    for s in stats:
        sys.stderr.write(s + "\n")


if __name__ == "__main__":
    main(sys.argv[1])
