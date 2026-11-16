-- 하프톤 API 수동 테스트용 초기 데이터 (local / halfthon 프로필).
-- 예약 ID: 사용자 1, 프로젝트/제작 기준 1, 요청 1~8.
-- 해당 ID가 비어 있는 개발 DB에서 사용한다. 이미 존재하는 ID의 데이터는 변경하지 않는다.
-- 재실행 시 같은 ID를 추가하지 않으며, 기존 요청/분석 이력을 삭제하지 않는다.
-- 기대 결과와 질문은 검증용 주석이며 AI 입력이나 분석 결과로 저장하지 않는다.

INSERT INTO users (id, kakao_id, email, display_name, created_at, updated_at)
SELECT 1, 900000000001, 'halfthon@example.com', '하프톤 테스트 사용자', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

INSERT INTO projects (id, owner_id, name, client_name, description, status, created_at, updated_at)
SELECT 1, 1, 'LUMEN 남성 향수 SNS 광고', 'LUMEN',
       '사전 설정된 제작 기준으로 클라이언트 요청을 분석하는 하프톤 테스트 프로젝트',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE id = 1);

INSERT INTO criteria_versions (id, project_id, version_no, criteria_text, created_at, updated_at)
SELECT 1, 1, 1, '[영상]
- 15초
- 9:16 세로형
- Instagram Reels 광고

[스토리 / 구성]
- 모델 등장
- 제품 사용 장면
- 마지막 제품 클로즈업

[분위기 / 스타일]
- 어둡고 시네마틱한 분위기
- 고급스럽고 차분한 톤
- 저채도 중심

[인물]
- 30대 한국인 남성
- 검은 정장
- 차분하고 진지한 인상

[제품 / 브랜드]
- 실제 제품 형태 유지
- 로고 형태와 위치 유지
- 제품 비율 임의 변경 금지

[카메라 / 연출]
- 제품 클로즈업 중심
- 안정적이고 느린 카메라 움직임

[레퍼런스]
- 전달받은 레퍼런스는 색감과 분위기만 참고
- 인물·구도는 그대로 따라 하지 않음

[원고]
- 현재 확정된 원고 사용', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM criteria_versions WHERE id = 1);

-- Case 1 - 기존 기준 변경
-- 기대 결과: 기존 검은 정장 / 차분하고 진지한 인상까지 변경하는 의미인지 확인 필요.
-- 기대 질문/안내: 말씀하신 캐주얼한 느낌은 모델 의상 변경도 포함할까요?
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 1, 1, '모델을 좀 더 밝고 캐주얼한 느낌으로 바꿔주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 1);

-- Case 2 - 모호한 표현
-- 기대 결과: 트렌디가 색감, 카메라, 편집, 의상 중 무엇을 의미하는지 불명확.
-- 기대 질문/안내: 트렌디하게 바꾸고 싶은 부분이 색감, 카메라 연출, 의상 중 어느 쪽에 가까울까요?
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 2, 1, '전체적으로 조금 더 트렌디하게 해주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 2);

-- Case 3 - 정보 누락
-- 제공된 기획대로 Case 2와 동일한 입력을 별도 요청으로 유지.
-- 기대 결과: 트렌디가 색감, 카메라, 편집, 의상 중 무엇을 의미하는지 불명확.
-- 기대 질문/안내: 트렌디하게 바꾸고 싶은 부분이 색감, 카메라 연출, 의상 중 어느 쪽에 가까울까요?
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 3, 1, '전체적으로 조금 더 트렌디하게 해주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 3);

-- Case 4 - 기존 기준 충돌
-- 기대 결과: 밝은 배경과 기존 어두운 시네마틱 분위기 사이 우선순위 확인 필요.
-- 기대 질문/안내: 배경을 밝게 하는 것과 현재의 어두운 시네마틱 분위기를 유지하는 것 중 어느 쪽이 더 중요할까요?
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 4, 1, '배경은 밝게 바꾸되 지금의 어두운 분위기는 그대로 유지해주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 4);

-- Case 5 - 작은 변경이 큰 영향을 줄 수 있는 요청
-- 기대 결과: 모델이 등장하는 모든 장면에 적용할 것인지 확인 필요.
-- 기대 질문/안내: 목걸이는 모델이 등장하는 모든 장면에 넣을까요, 아니면 특정 장면에만 넣을까요?
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 5, 1, '모델한테 목걸이 하나만 추가해주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 5);

-- Case 6 - 추가 장면
-- 기대 결과: 장면 길이와 내용이 없음.
-- 기대 질문/안내: 추가할 제품 소개 장면은 몇 초 정도로, 어떤 내용을 보여주면 될까요?
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 6, 1, '마지막에 제품 소개 장면 하나 더 넣어주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 6);

-- Case 7 - 매우 짧은 요청
-- 기대 결과: 수정 대상 자체가 없음. AI 실패로 처리하지 않음.
-- 기대 질문/안내: 어떤 부분을 수정하고 싶으신가요? 인물, 배경, 분위기, 제품 중 가장 먼저 바꾸고 싶은 부분을 알려주세요.
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 7, 1, '좀 수정해주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 7);

-- Case 8 - 분석 결과 없어야 하는 요청
-- 기대 결과: 추가 확인할 중요한 분석 결과 없음. COMPLETED와 findings: [] 기대.
-- 기대 질문/안내: 현재 제작 기준과 비교했을 때 추가로 확인할 중요한 내용을 찾지 못했어요.
INSERT INTO client_requests (id, project_id, request_text, created_at, updated_at)
SELECT 8, 1, '마지막 제품 클로즈업 장면에서 로고가 조금 더 잘 보이게 위치만 조정해주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_requests WHERE id = 8);
