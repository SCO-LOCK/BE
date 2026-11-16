# SCO/LOCK -BE

제작 기준과 클라이언트의 새 요청을 비교해, 제작 전에 확인할 사항과 질문 초안을 생성하는 백엔드입니다.

## 🛠️ 개발 환경

| 구분        | 기술 및 버전                              |
|:------------|:------------------------------------------|
| Language    | Java 21                                   |
| Framework   | Spring Boot 4.1.2                         |
| Build Tool  | Gradle 9.8.2 (Wrapper)                    |
| Web         | Spring Web MVC                            |
| Persistence | Spring Data JPA                           |
| Database    | MySQL (`local`, `halfthon`) · H2 (테스트) |
| AI          | Spring AI 2.0.2 · OpenAI                  |
| API Docs    | springdoc-openapi 3.2.0                   |
| Validation  | Spring Boot Starter Validation            |
| Library     | Lombok                                    |
| Test        | Spring Boot Test · JUnit Platform         |

## 로컬 실행 및 테스트

- 활성 프로필을 지정하지 않으면 `local` 프로필을 사용합니다.
- Java 21과 MySQL이 필요합니다. 로컬 접속 대상은 `localhost:3306/scolock_db`이며, 실행 전에 데이터베이스를 생성하고 접속 계정을 준비해야 합니다.
- `DB_USERNAME`, `DB_PASSWORD`, `OPENAI_API_KEY`를 실행 환경 변수로 설정합니다. 현재 설정에서는 애플리케이션 시작 시에도 API 키 설정이 필요합니다.
- IntelliJ 실행 구성에서도 환경 변수 이름 앞뒤에 공백이 없어야 합니다. 예를 들어 ` DB_PASSWORD`는 `DB_PASSWORD`와 다른 이름이므로 비밀번호가 전달되지 않습니다.
- `.env`를 자동으로 불러오는 설정은 없습니다. 터미널 환경 변수 또는 IDE 실행 구성에 값을 지정합니다.
- 개발용 스키마는 `ddl-auto=update`로 갱신하며, 초기 데이터는 아래 하프톤 초기 데이터 절을 참고하세요.
- DB를 사용하는 자동 테스트는 `test` 프로필의 H2 메모리 DB와 `ddl-auto=create-drop`을 사용합니다. 테스트 설정에서 AI 키를 대체하므로 MySQL과 실제 OpenAI API 키 없이
  실행할 수 있습니다.

macOS / Linux:

```bash
# 애플리케이션 실행에는 DB_USERNAME, DB_PASSWORD, OPENAI_API_KEY가 필요합니다.
./gradlew bootRun
./gradlew test
```

Windows PowerShell:

```powershell
# 애플리케이션 실행에는 DB_USERNAME, DB_PASSWORD, OPENAI_API_KEY가 필요합니다.
.\gradlew.bat bootRun
.\gradlew.bat test
```

`halfthon` 프로필로 실행하려면 `SPRING_PROFILES_ACTIVE=halfthon`과 MySQL JDBC URL인 `DB_URL`을 추가로 설정합니다.
DB 계정과 AI 키는 로컬과 동일한 환경 변수를 사용합니다. 기본 포트는 `8081`이며, `halfthon`에서는 `PORT`로 변경할 수 있습니다.

`Unable to determine Dialect without JDBC metadata`가 발생하면 앞선 로그의 JDBC 연결 오류를 확인하고 MySQL 실행 여부, 데이터베이스 존재 여부, 환경 변수 이름과
인증 정보를 확인합니다. 연결 실패 상태에서 dialect만 지정해도 DB 연결 문제는 해결되지 않습니다.

## Swagger API 문서

- `springdoc-openapi-starter-webmvc-ui:3.2.0`을 사용합니다.
- 애플리케이션 실행 후 Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- `@RestController`에 추가한 API는 자동으로 문서화됩니다.
- API 설명은 `@Operation`, 컨트롤러 그룹 이름은 `@Tag`, DTO 필드 설명은 `@Schema`로 추가할 수 있습니다.

## 커밋 메시지

- feat:      새로운 기능 구현
- fix:       버그, 오류 해결
- chore:     src 또는 test 파일을 수정하지 않는 기타 변경 사항, 자잘한 수정 (새로운 파일 생성, 파일 이동, 이름 변경 등 )
- refactor:  버그 수정이나 기능 추가가 없는 코드 변경 (코드 구조 변경 등의 리팩토링 )
- build:     빌드 시스템 또는 외부에 영향을 미치는 변경 사항 종속성 (라이브러리 추가 등 )
- perf:      성능을 향상 시키기 위한 코드 변경
- test:      테스트 추가 또는 이전 테스트 수정
- docs:      README나 WIKI 등의 문서 개정
- revert:    이전 커밋을 되돌리는 경우
- merge:     다른브렌치를 merge하는 경우
- init :     Initial commit을 하는 경우

## AI 요청 점검

1. `POST /projects/{projectId}/requests`에 `{"requestText":"새 클라이언트 요청 원문"}`을 보내 원문을 저장합니다.
2. 응답의 `data.id`를 요청 ID로 사용해 `POST /projects/{projectId}/requests/{requestId}/analyses`를 호출합니다. 본문은 없습니다.
3. 이 API는 분석 기록을 `PENDING`으로 저장하고 **HTTP 202**와 `analysisId`로 접수 응답을 반환합니다. 해당 프로젝트에서 `criteria_versions.version_no`가 가장
   높은 기준과 저장된 요청을 사용하는 AI 분석은 별도 스레드에서 실행합니다. 워커가 실행을 시작하면 DB 상태를 `PROCESSING`으로 변경하고 `started_at`을 기록합니다.

```json
{
  "code": "COMMON202",
  "message": "요청이 접수되었습니다.",
  "data": {
    "analysisId": 1
  }
}
```

- 생성 응답의 `data`는 `analysisId`만 포함합니다. `GET /analyses/{analysisId}`로 현재 상태를 조회하며, `COMPLETED` 상태일 때 상세 결과를 반환합니다. 실패 시 공개용 오류 코드와 메시지를 반환합니다.
- 저장되는 결과 유형은 `MISSING_INFO`, `AMBIGUOUS`, `BASELINE_CHANGE`, `TECH_CHECK`이며, 요청 원문 인용 필드는 `request_excerpt`입니다.
- 확인사항은 최대 3개입니다. 기존 기준과 직접 충돌하거나 변경 가능성이 큰 내용 → 필수 정보 누락 → 모호한 표현 → 기술 확인 순으로 판단하고 선택합니다.
  확인 없이 진행하면 실제 방향 오류나 재작업 가능성이 있는 내용만 포함하며, 사소한 확인사항과 중복은 제외합니다. 3개를 억지로 채우지 않습니다.
  AI 출력 스키마와 서버 검증에 모두 개수 제한을 적용하며, 초과 응답은 `AI_INVALID_RESPONSE`로 처리합니다.
- 확인사항이 없는 경우 DB 상태를 `COMPLETED`로 변경하고 빈 `findings`를 저장합니다. 입력 정보 부족 자체는 시스템 오류가 아닙니다.
- AI 실패 시 DB 상태를 `FAILED`로 변경하고 오류 코드 (`AI_API_ERROR`, `AI_TIMEOUT`, `AI_INVALID_RESPONSE`)와 메시지를 저장합니다. 그 외 처리 오류는
  `ANALYSIS_ERROR`로 저장합니다. 접수 응답은 분석 성공을 의미하지 않습니다.
- 작업 큐가 가득 차 접수하지 못하면 해당 분석을 `FAILED`, 오류 코드 `ANALYSIS_UNAVAILABLE`로 저장하고 HTTP 503을 반환합니다.
- 실패해도 요청 원문은 보존되며 같은 POST를 다시 호출하면 새 분석 기록으로 재시도합니다. 같은 요청에 대기·진행 중인 분석이 있으면 HTTP 409 (`ANALYSIS409`), 완료된 분석이 있으면 HTTP
  409 (`ANALYSIS409_COMPLETED`)를 반환합니다. 완료된 분석이 있으면 제작 기준이 변경되거나 실패 이력이 있어도 재분석하지 않습니다.
- 분석을 기다리는 동안 DB 트랜잭션을 열어 두지 않습니다. 서버가 강제 종료되어 `PENDING` 또는 `PROCESSING` 기록이 남은 경우에는 해당 기록을 운영자가 실패 처리한 뒤 재시도해야 합니다.
- 결과 전체는 `analysis_runs.result_json`에 저장하고, `findings`의 각 항목은 같은 트랜잭션에서 `analysis_items`에 저장합니다.
  각 항목은 `analysis_run_id`로 분석과 연결되며, `item_order`는 위 우선순위로 정렬한 뒤 1부터 부여합니다. 같은 유형 안에서는 AI의 판단 순서를 유지합니다.
  `title`, `request_excerpt`, `reason`, `suggested_question`은 그대로 저장하고, `type`은 `classification`,
  `existing_baseline`은 `criteria_evidence`에 저장합니다. 빈 `findings`는 항목을 생성하지 않고 분석을 완료합니다.
- UI의 Loading/CTA 비활성화, 질문 복사, 화면 이동은 프런트엔드에서 처리합니다. 파일 첨부는 하프톤 제외 조건에 따라 포함하지 않습니다.
- 현재 기존 API와 동일하게 사용자 ID 1을 사용합니다.

실행 환경 변수:

| 변수                  | 설명                                                                      |
|-----------------------|---------------------------------------------------------------------------|
| `OPENAI_API_KEY`      | OpenAI API 키 (필수)                                                      |
| `OPENAI_MODEL`        | 분석에 사용할 모델. 기본값 `gpt-4o-mini`                                  |
| `AI_ANALYSIS_TIMEOUT` | 워커의 AI 호출 제한 시간. 기본값 `60s`. HTTP 접수 응답의 대기 시간이 아님 |

`OPENAI_MODEL`은 YAML의 `spring.ai.openai.chat.model`에 연결되며, Spring AI와 분석 클라이언트가 같은 설정을 사용합니다.

응답 형식은 [OpenAI Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs)를 사용하며 필수 값, 유형, 원문
인용과 완료 여부를 추가 검증합니다. 비용·책임·작업시간·크레딧·범위 내/외를 확정하지 않도록 시스템 프롬프트를 구성했습니다. 이는 모델에 대한 지시이므로 생성된 질문과 근거는 제작자가 확인해야 합니다. 자동 발송
기능은 없습니다.

테스트에서는 AI를 대체하여 실제 API 호출과 과금 없이 성공, 빈 결과, 응답 형식 오류, 시간초과, 접근 권한, 중복 실행, 재시도를 검증합니다.

## 하프톤 API 테스트 초기 데이터

`src/main/resources/data.sql`은 `local`, `halfthon` 프로필에서 Hibernate 스키마 생성/갱신 후 UTF-8로 자동 실행됩니다.
일반 자동 테스트의 `test` 프로필에서는 실행하지 않습니다. 현재 두 실행 프로필은 MySQL을 사용합니다.

| 데이터    | ID  | 내용                                             |
|-----------|-----|--------------------------------------------------|
| 사용자    | 1   | 현재 API의 임시 사용자 (이미 있으면 그대로 사용) |
| 프로젝트  | 1   | LUMEN 남성 향수 SNS 광고                         |
| 제작 기준 | 1   | 버전 1, 8개 항목의 전체 기준 텍스트              |
| 요청      | 1~8 | 제공된 Case 1~8 순서                             |

초기 데이터용 ID가 비어 있는 개발 DB에서 사용하세요. 재시작 시 이미 있는 ID는 건너뛰며 기존 데이터와 분석 이력은 변경하지 않습니다.
Case 2와 3은 기획에 적힌 동일한 문구를 각각 별도 요청으로 저장했습니다. 기대 결과와 질문은 SQL 주석에만 기록합니다.
분석 기록은 미리 만들지 않으므로 실제 AI 분석 API를 바로 호출할 수 있습니다.

```bash
# Case 1 분석 (본문 없음). Case 2~8은 requestId를 2~8로 변경합니다.
curl -X POST http://localhost:8081/projects/1/requests/1/analyses

# 요청 등록 API를 직접 테스트할 때
curl -X POST http://localhost:8081/projects/1/requests \
  -H 'Content-Type: application/json' \
  -d '{"requestText":"모델한테 목걸이 하나만 추가해주세요."}'
```

분석 호출에는 `OPENAI_API_KEY`가 필요합니다. 당일 검증 기준의 30초 제한은 `AI_ANALYSIS_TIMEOUT=30s`로 설정할 수 있습니다.
Case 8은 접수 후 DB 상태 `COMPLETED`와 빈 `findings`를 기대하며, 안내 문구는 FE에서 표시합니다.
각 결과는 문제 탐지 여부, 불필요한 경고 여부, 근거의 납득 가능성, 질문의 실제 전달 가능성으로 확인합니다.
