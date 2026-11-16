# SCO/LOCK -BE

## 🛠️ 개발 환경

| 구분 | 기술 및 버전 |
| :--- | :--- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle 9.8.2 (Wrapper) |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA |
| Database | MySQL (로컬 개발), H2 (테스트) |
| Validation | Spring Boot Starter Validation |
| Library | Lombok |
| Test | Spring Boot Test · JUnit Platform |

## 로컬 실행 및 테스트

- 활성 프로필을 지정하지 않으면 `local` 프로필을 사용합니다.
- 로컬 실행에는 `localhost:3306`에서 실행 중인 MySQL과 `scolock_db` 데이터베이스가 필요합니다. 데이터베이스는 미리 생성해야 하며, 개발용 테이블은 `ddl-auto=update`로 갱신합니다.
- 실행 프로세스에 `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 설정합니다. IntelliJ 실행 구성에서도 변수 이름 앞뒤에 공백이 없어야 합니다. 예를 들어 ` DB_PASSWORD`는 `DB_PASSWORD`와 다른 이름이므로 비밀번호가 전달되지 않습니다.
- `.env` 파일은 자동으로 로드하지 않습니다. IntelliJ 실행 구성 또는 터미널에서 환경 변수를 전달해야 합니다.
- 테스트는 `test` 프로필의 별도 메모리 DB와 `ddl-auto=create-drop`을 사용합니다.
- `halfthon` 프로필은 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경 변수로 MySQL 연결을 설정합니다.

```powershell
# 현재 터미널에 DB_USERNAME, DB_PASSWORD를 설정한 후 실행합니다.
.\gradlew.bat bootRun
.\gradlew.bat test
```

`Unable to determine Dialect without JDBC metadata`가 발생하면 앞선 로그의 JDBC 연결 오류를 확인하고 MySQL 실행 여부, 데이터베이스 존재 여부, 환경 변수 이름과 인증 정보를 확인합니다. 연결 실패 상태에서 dialect만 지정해도 DB 연결 문제는 해결되지 않습니다.

## Swagger API 문서

- Spring Boot 4.1.x와 호환되는 `springdoc-openapi-starter-webmvc-ui:3.2.0`을 사용합니다.
- 애플리케이션 실행 후 Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- `@RestController`에 추가한 API는 자동으로 문서화됩니다. 아직 컨트롤러가 없다면 API 목록은 비어 있습니다.
- API 설명은 `@Operation`, 컨트롤러 그룹 이름은 `@Tag`, DTO 필드 설명은 `@Schema`로 추가할 수 있습니다.

## 커밋 메시지
- feat:      새로운 기능 구현
- fix:       버그, 오류 해결
- chore:     src 또는 test 파일을 수정하지 않는 기타 변경 사항, 자잘한 수정 ( 새로운 파일 생성, 파일 이동, 이름 변경 등 )
- refactor:  버그 수정이나 기능 추가가 없는 코드 변경 ( 코드 구조 변경 등의 리팩토링 )
- build:     빌드 시스템 또는 외부에 영향을 미치는 변경 사항 종속성 ( 라이브러리 추가 등 )
- perf:      성능을 향상 시키기 위한 코드 변경
- test:      테스트 추가 또는 이전 테스트 수정
- docs:      README나 WIKI 등의 문서 개정
- revert:    이전 커밋을 되돌리는 경우
- merge:     다른브렌치를 merge하는 경우
- init :     Initial commit을 하는 경우
