# SCO/LOCK -BE

## 🛠️ 개발 환경

| 구분 | 기술 및 버전 |
| :--- | :--- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle 9.8.2 (Wrapper) |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA |
| Database | H2 (로컬 개발 및 테스트) |
| Validation | Spring Boot Starter Validation |
| Library | Lombok |
| Test | Spring Boot Test · JUnit Platform |

## 로컬 실행 및 테스트

- 별도 DB 설치 없이 실행할 수 있도록 H2 JDBC 드라이버를 포함합니다.
- 활성 프로필을 지정하지 않으면 `local` 프로필을 사용합니다.
- 로컬 DB는 `./.local/data/scolock`에 파일로 저장되며 재시작 후에도 유지됩니다. 해당 디렉터리는 Git 추적에서 제외됩니다.
- 로컬 H2 접속 정보는 사용자 `sa`, 빈 비밀번호이며, 개발용 스키마는 `ddl-auto=update`로 갱신합니다.
- 테스트는 `test` 프로필의 별도 메모리 DB와 `ddl-auto=create-drop`을 사용합니다.
- 운영 DB 도입 시 해당 JDBC 드라이버와 프로필별 연결 설정 및 스키마 관리 정책을 별도로 구성해야 합니다.

```powershell
.\gradlew.bat bootRun
.\gradlew.bat test
```

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
