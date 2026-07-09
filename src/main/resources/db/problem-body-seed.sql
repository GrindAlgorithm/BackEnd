-- 문제 본문/예제 시드 (IDE 진입 시 노출, 연동 문서 §2.8)
-- 출처: ICPC North America Rocky Mountain Regional 2020
--   https://github.com/icpc/na-rocky-mountain-2020-public
-- 자동 생성(gen_body_seed.py). problem-body-schema.sql + problem-list-seed.sql 선행 필요.

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Kayla monitors antimatter rainfall on an exotic planet using sensors arranged in a 2D section. Antimatter droplets fall from above at the same rate. When one or more droplets hit a sensor, both the droplets and the sensor simultaneously disappear. Remaining droplets continue falling past the destroyed sensor''s location. Each sensor is either a horizontal strip or a point. For each droplet, determine the y-coordinate where it will disintegrate, or output 0 if it never hits any sensor.', 'The first line contains two integers D (1 <= D <= 100000), the number of antimatter droplets, and S (1 <= S <= 100000), the number of sensors.
The next D lines each contain two integers x and y (1 <= x, y <= 10^9), a droplet''s coordinates.
The next S lines each contain three integers x1, x2, y (1 <= x1 <= x2 <= 10^9): a sensor''s left x, right x, and y.', 'For each droplet in order, output a single integer: the y-coordinate where it disintegrates, or 0 if it never hits any sensor.' FROM problem WHERE problem_id = 'antimatterrain';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '5 3
1 8
2 3
2 8
5 8
5 9
3 6 6
1 7 4
1 3 1', '4
1
4
6
0' FROM problem WHERE problem_id = 'antimatterrain';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Arithmetic coding encodes a message as a real number between 0 and 1. Given a message of only ''A''s and ''B''s with probabilities p_A and p_B = 1 - p_A, the encoding repeatedly subdivides an interval. Starting with [0, 1), for each letter compute c = a + p_A*(b - a). If the letter is ''A'', the new interval is [a, c); otherwise it is [c, b). After all letters, the encoded value is the left endpoint of the final interval. Given the encoded message and its parameters, decode it back to the original message.', 'The first line contains an integer N (1 <= N <= 15), the length of the original message. The second line contains an integer D (1 <= D <= 7), where p_A = D/8. The third line contains the binary representation of the encoded message, starting with "0." and containing at most 3N + 2 characters.', 'Display the original message as a string of ''A'' and ''B'' characters.' FROM problem WHERE problem_id = 'arithmeticdecoding';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '4
4
0.0101', 'ABAB' FROM problem WHERE problem_id = 'arithmeticdecoding';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'The Spanning Nation begins occupying island 1 and can attack adjacent islands. An attack succeeds only if the target island''s army is strictly smaller than the Spanning Nation''s current army size. When conquered, the island''s army joins the Spanning Nation''s forces. Given a connected graph of islands with bridges and army sizes, determine the maximum total army the Spanning Nation can accumulate through an optimal sequence of conquests.', 'The first line contains two integers N (1 <= N <= 200000), the number of islands, and M (0 <= M <= 200000), the number of bridges.
The next M lines each contain two distinct integers u and v (1 <= u, v <= N), a bridge between islands u and v.
The next N lines each contain a single integer s (0 <= s <= 1000), the army size of islands 1..N in order.', 'Display the maximum possible army size of the Spanning Nation after all possible attacks.' FROM problem WHERE problem_id = 'conquest';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '6 5
1 4
3 4
2 4
6 3
5 4
2
4
1
0
10
2', '9' FROM problem WHERE problem_id = 'conquest';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Manhattan''s street grid uses numbered streets (running North-South) and avenues (running East-West). Each intersection is identified by coordinates (s, a). The distance between two intersections is the Manhattan distance |s1-s2| + |a1-a2|. A company operates food trucks at various intersections and wants to compute the combined distance across all distinct pairs of food trucks.', 'The first line contains an integer N (2 <= N <= 200000), the number of food trucks. The following N lines each contain two integers s (1 <= s <= 1000000) and a (1 <= a <= 1000000): the street and avenue of each truck.', 'Output the sum of Manhattan distances between all distinct pairs of food trucks.' FROM problem WHERE problem_id = 'distance';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '3
1 1
4 5
2 3', '14' FROM problem WHERE problem_id = 'distance';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Puff the Magic Dinosaur performs a card trick. He lays out N cards labeled 1 through N and secretly predicts one card''s label. The princess repeatedly selects some cards from those remaining. At each step, Puff must decide whether the chosen cards are kept (removing all others) or removed (keeping all others). This continues until one card remains, which must match Puff''s prediction. Determine the correct response at each step.', 'The first line contains three integers N (2 <= N <= 200, cards), P (1 <= P <= N, prediction), and S (1 <= S <= N-1, steps). Each of the next S lines begins with m (1 <= m <= N-1, chosen count) followed by m distinct card labels. The princess selects from cards still on the table, always at least one but not all remaining.', 'For each of the S steps, output KEEP or REMOVE on its own line.' FROM problem WHERE problem_id = 'forcedchoice';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '10 3 4
2 1 5
5 2 3 7 8 10
3 2 7 10
1 8', 'REMOVE
KEEP
REMOVE
REMOVE' FROM problem WHERE problem_id = 'forcedchoice';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Candidates stand in a queue for interviews. Every minute, each candidate simultaneously checks the perceived value of their adjacent neighbors (ahead and behind). If at least one neighbor has a value strictly greater than their own, they leave the queue. All who decide to leave do so simultaneously. This repeats until no more candidates leave. Determine how many minutes elapse, which candidates leave each round, and the final queue state.', 'The first line contains an integer N (1 <= N <= 100000), the number of candidates. The second line contains N integers v_1..v_N (0 <= v_i <= 10^9), the perceived values in queue order.', 'Display M, the number of minutes during which candidates leave. Then M lines: the i-th line lists the values of candidates who left in minute i, in queue order. Finally, display the values of the remaining queue in order.' FROM problem WHERE problem_id = 'interviewqueue';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '10
3 6 2 3 2 2 2 1 5 6', '2
3 2 2 1 5
3 2 2
6 6' FROM problem WHERE problem_id = 'interviewqueue';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Samantha makes paper snowflakes by folding a strip and cutting. A strip of length L picometres has one end fixed at 0 and the loose end at L. She performs N folds; fold i creases f_i picometres from the loose end and folds over. Odd-numbered folds go left, even-numbered folds go right. After all folds she makes M cuts at given positions. Determine the total length of paper in each of the M+1 resulting piles.', 'First line: three integers N (1 <= N <= 10^5), M (1 <= M <= 10^5), L (2 <= L <= 10^18). Second line: N integers with 1 <= f_N < ... < f_1 < L (fold positions from the loose end). Third line: M integers with -10^18 <= c_1 < ... < c_M <= 10^18 (cut positions from the fixed point).', 'Output M+1 integers: the total paper length in each pile from left to right.' FROM problem WHERE problem_id = 'papersnowflakes';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '4 2 20
19 17 11 7
1 6', '5 13 2' FROM problem WHERE problem_id = 'papersnowflakes';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Pegs and Legs is a game where a disk slides down a board toward landing spots called legs, each worth points. A disk dropped on a peg falls left with probability l, right with probability r, or gets stuck with probability 1 - l - r. If stuck, it must be dropped again from a top position. The disk continues until landing in a leg, earning that leg''s value. Find the maximum expected score achievable through optimal choice of drop points.', 'First line: two integers L (1 <= L <= 100000, legs) and P (1 <= P <= 100000, pegs). Legs are numbered 1..L; pegs are numbered L+1..L+P. Next L lines each contain integer v (1 <= v <= 1000000), a leg value. Next P lines each contain two real numbers l and r (0 < l < 1, 0 < r < 1, l + r <= 1) and two integers x and y (destinations when falling left or right), both with smaller labels than the current peg. Reals have exactly 3 decimals.', 'Display the maximum possible expected score. Answers within absolute or relative error 10^-6 are accepted.' FROM problem WHERE problem_id = 'pegsandlegs';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '2 4
344969
539194
0.508 0.318 1 1
0.990 0.009 1 3
0.807 0.041 3 1
0.225 0.617 4 4', '539194.0000000000' FROM problem WHERE problem_id = 'pegsandlegs';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'Robin has a stopwatch with a single button that toggles between running and stopped. Initially it is stopped and displays 0 seconds. Each second the timer runs, the displayed time increases by 1. Given the times of the button presses, determine the final state of the stopwatch.', 'The first line contains an integer N (1 <= N <= 1000), the number of button presses. The next N lines each contain an integer t (0 <= t <= 1000000), the time of a press, in increasing order with no duplicates.', 'If the stopwatch is still running after all presses, print "still running". Otherwise print the number of seconds displayed.' FROM problem WHERE problem_id = 'stopwatch';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '2
7
11', '4' FROM problem WHERE problem_id = 'stopwatch';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'A train has N cars numbered 1 to N (front to back), each L meters long, with one door at each car''s center and no gaps between cars. When the train stops, each passenger walks to the nearest door, choosing the higher-numbered car if two doors are equidistant. Given passenger locations, determine the longest walking distance for any passenger and the maximum number of passengers boarding a single car.', 'The first line contains three integers N (1 <= N <= 100, cars), L (2 <= L <= 100, even car length in meters), and P (1 <= P <= 1000, passengers). Each of the next P lines contains one integer x (0 <= x <= 10000), the distance behind the train''s front end where a passenger stands.', 'On the first line, the longest distance any passenger must walk to a door. On the second line, the maximum number of passengers boarding any single car.' FROM problem WHERE problem_id = 'trainboarding';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '68 90 6
3809
288
2306
2215
4211
379', '27
1' FROM problem WHERE problem_id = 'trainboarding';

INSERT INTO problem_body (problem_id, description, input_spec, output_spec)
SELECT id, 'A clinical trial tests vaccine efficacy against three virus strains (A, B, C). Participants are either vaccinated or given a placebo. Vaccine efficacy is the percentage reduction in infection rate between the vaccinated and control groups. If the vaccinated group''s infection rate is not lower than the control group''s, the vaccine is ineffective for that strain. Example: 8/40 vaccinated (20%) vs 30/50 control (60%) gives about 66.67% efficacy.', 'The first line contains an integer N (2 <= N <= 10000), the number of participants. Each of the next N lines contains a four-character string of ''Y'' or ''N''. The first character is vaccination status (Y = vaccinated, N = control); the remaining three indicate infection by strains A, B, C. At least one participant is in each group, and the control group has at least one infected person per strain.', 'Display the vaccine efficacy percentage against strains A, B, and C in order. For any strain where the vaccine is ineffective, display "Not Effective" instead. Answers within 10^-2 error are accepted.' FROM problem WHERE problem_id = 'vaccineefficacy';
INSERT INTO problem_sample (problem_id, ordinal, input, output)
SELECT id, 1, '10
NYNY
NNYY
NYYY
NYYN
NNNY
YYNN
YYYN
YYNY
YYNN
YYNY', 'Not Effective
66.666667
50.000000' FROM problem WHERE problem_id = 'vaccineefficacy';

