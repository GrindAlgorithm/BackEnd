-- 친구(팔로우) 관계 — 랭킹 탭 '친구' 스코프(연동 문서 §2.13)의 소스.
-- owner_handle(관계 소유자=나)이 friend_handle 을 친구로 추가한 단방향 행이다.
-- '친구 랭킹'은 내가 추가한 friend_handle 들의 현재 시즌 랭킹을 보여준다.
--
-- season_ranking 과 동일하게 handle 문자열로 식별한다(유저 도메인 연동 전 데모 데이터와
-- 매칭돼야 하므로 app_user FK 를 걸지 않는다 — season_ranking 주석과 동일한 이유).
CREATE TABLE IF NOT EXISTS friendship (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_handle  VARCHAR(64) NOT NULL,          -- 관계 소유자 handle (app_user.handle)
    friend_handle VARCHAR(64) NOT NULL,          -- 친구 handle (season_ranking.handle 과 매칭)
    created_at    DATETIME    NOT NULL,
    CONSTRAINT uq_friendship UNIQUE (owner_handle, friend_handle),
    CONSTRAINT chk_friendship_not_self CHECK (owner_handle <> friend_handle)
);

-- 친구 목록 조회(owner_handle 기준)용 인덱스
CREATE INDEX idx_friendship_owner ON friendship (owner_handle);
