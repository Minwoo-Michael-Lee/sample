# Tomcat 배포 가이드 (인턴용)

이 문서는 **`WAS/tomcat-app` 프로젝트를 Tomcat에 배포**하고, **`index.html`이 어떻게 열리는지**, 그리고 **정적 파일(HTML/CSS/JS/이미지) / API(Java Servlet)**가 어떻게 연결되는지 “인턴 기준”으로 설명합니다.

## TL;DR (5분 요약)

- **Tomcat에 배포하는 것**: `neis-parents-service.war` (또는 압축이 풀린 폴더 형태 “exploded”)  
- **메인 화면(`/`)**: `web.xml`의 welcome-file 때문에 `index.html`이 열림  
- **정적 파일**: `src/main/webapp/**` 아래에 둔 파일이 URL로 그대로 나감  
- **API(서블릿)**: `WEB-INF/web.xml`에서 `/api/...` → `com.neis.servlet.*Servlet`로 매핑됨  
- **DB 설정**: 환경변수 `NEIS_DB_URL/USER/PASSWORD` 또는 `web.xml context-param`로 설정  

---

## 0) 배포 흐름(인턴 체크리스트)

1. (처음 1회) **JDK 11 + Tomcat 9 + Maven** 설치/설정
2. `WAS/tomcat-app`에서 **WAR 생성**
3. 생성된 WAR를 Tomcat의 `webapps/`에 **복사**
4. Tomcat 실행 → 브라우저로 접속
5. 에러 나면 **Tomcat 로그(`logs/`)**부터 확인

---

## 1) Tomcat에는 뭘 배포하나요?

Tomcat은 **Java 웹 애플리케이션(WAR)** 을 배포하는 WAS입니다.

이 프로젝트(`WAS/tomcat-app`)는 Maven WAR 프로젝트라서, 배포 산출물은:

- **`neis-parents-service.war`** (WAR 파일)

WAR 안에는 크게 2종류가 들어갑니다.

- **정적 리소스**: `index.html`, `login.html`, `css/style.css`, `js/main.js`, `images/*` 등  
  → 브라우저가 바로 요청하면 파일 그대로 내려줌
- **Java 코드(서블릿/필터)**: `/api/...` 같은 URL을 처리  
  → DB 조회/처리 후 JSON 응답

---

## 2) 프로젝트 구조(중요)

Tomcat 배포 기준으로 이 폴더가 핵심입니다.

- **정적 리소스 루트**: `WAS/tomcat-app/src/main/webapp/`
  - 예) `src/main/webapp/index.html`
  - 예) `src/main/webapp/css/style.css`
  - 예) `src/main/webapp/images/banner-img2.png`
  - 예) `src/main/webapp/student-attendance.html`
  - 예) `src/main/webapp/WEB-INF/web.xml` (**중요: 설정 파일**)

- **Java 소스**: `WAS/tomcat-app/src/main/java/`
  - 예) `src/main/java/com/neis/servlet/AttendanceServlet.java`
  - 예) `src/main/java/com/neis/db/DBConnection.java`

빌드되면 다음처럼 매핑됩니다.

- `src/main/webapp/**` → WAR 루트(`/`)
- `src/main/webapp/WEB-INF/**` → 브라우저에서 직접 접근 불가(서버 설정 영역)
- `src/main/java/**` → 컴파일되어 `WEB-INF/classes/**`로 들어감

---

## 3) `index.html`은 어떻게 결정되나요?

브라우저가 사이트 루트로 들어오면:

- URL: `/` (예: `http://localhost:8080/neis-parents-service/`)

Tomcat은 “웰컴 파일(welcome file)” 목록을 보고, 그중 존재하는 파일을 응답합니다.

이 프로젝트는 `web.xml`에 이렇게 되어 있습니다:

- 파일: `WAS/tomcat-app/src/main/webapp/WEB-INF/web.xml`
- 설정: `<welcome-file>index.html</welcome-file>`

즉,

- `/` 요청 → `index.html` 반환

### (중요) `index.html`은 “Java가 아니라 정적 파일”입니다

`index.html`은 단순 HTML이라서:

- 화면/디자인 수정은 `src/main/webapp/index.html` 수정
- API 연동은 JS에서 `fetch('api/...')` 호출

**주의:** Tomcat에 이미 WAR를 배포한 상태에서 소스만 바꾸면 서버에는 반영되지 않습니다.  
반영하려면 아래 중 하나를 해야 합니다:

- WAR를 다시 만들어서 재배포(가장 표준)
- 또는 `webapps/neis-parents-service/`처럼 “압축이 풀린 폴더(exploded)”의 파일을 직접 수정(개발용)

---

## 4) 배포 후 URL은 어떻게 되나요?

Tomcat은 보통 `webapps/` 아래 WAR 파일명을 기준으로 **컨텍스트 경로(Context Path)** 를 잡습니다.

예를 들어 `neis-parents-service.war`를 배포하면:

- 컨텍스트 경로: `/neis-parents-service`
- 메인: `http://localhost:8080/neis-parents-service/`
- 로그인 페이지: `http://localhost:8080/neis-parents-service/login.html`
- 출결상황 페이지: `http://localhost:8080/neis-parents-service/student-attendance.html`
- API(출결): `http://localhost:8080/neis-parents-service/api/attendance?...`

> “루트(/)로 바로 서비스”하고 싶으면 `ROOT.war`로 배포하거나, Tomcat 설정으로 컨텍스트를 ROOT로 맞추는 방식이 있습니다(운영 단계에서 적용).

---

## 5) 정적 파일(HTML/CSS/JS) 경로는 어떻게 잡나요?

### 5-1. 기본 규칙

`src/main/webapp` 아래 파일들은 **URL로 그대로 접근**할 수 있습니다.

예)

- 파일: `src/main/webapp/css/style.css`  
  → URL: `/neis-parents-service/css/style.css`

### 5-2. 왜 상대경로를 쓰나요?

HTML에서 링크를 이렇게 쓰는 걸 추천합니다:

- ✅ `css/style.css` (상대경로)
- ✅ `login.html`
- ✅ `api/attendance?...` (상대경로)

이렇게 하면 컨텍스트 경로가 `/neis-parents-service`든 `/`든 **코드 수정 없이 동작**합니다.

반대로 아래처럼 절대경로(`/...`)를 쓰면:

- ❌ `/css/style.css`

컨텍스트가 바뀌면 깨질 가능성이 큽니다.

---

## 6) API(Java Servlet)는 어떻게 연결되나요?

### 6-1. `web.xml` 매핑

API는 `web.xml`에 URL이 매핑됩니다.

- 파일: `WAS/tomcat-app/src/main/webapp/WEB-INF/web.xml`

예) 출결 API:

- `/api/attendance` → `com.neis.servlet.AttendanceServlet`

### 6-2. 프론트에서 호출 방법

출결 페이지(`student-attendance.html`)에서는 다음처럼 호출합니다:

- `fetch('api/attendance?...')`

즉, 페이지가 같은 컨텍스트 안에 있을 때:

- `/neis-parents-service/student-attendance.html`에서
- `/neis-parents-service/api/attendance`를 호출하는 형태입니다.

---

## 7) DB 연결은 어디서 설정하나요?

DB 접속 정보는 우선순위가 있습니다:

1) **환경변수(추천)**  
   - `NEIS_DB_URL`
   - `NEIS_DB_USER`
   - `NEIS_DB_PASSWORD`

2) `web.xml`의 `<context-param>`  
   - `NEIS_DB_URL`
   - `NEIS_DB_USER`
   - `NEIS_DB_PASSWORD`

관련 파일:

- `WAS/tomcat-app/src/main/java/com/neis/db/DBConfig.java`
- `WAS/tomcat-app/src/main/webapp/WEB-INF/web.xml`

---

## 8) 빌드(WAR 만들기) & 배포(서버에 올리기)

### 8-1. 준비물

- **JDK 11** 설치 (예: [Temurin JDK 11](https://adoptium.net/temurin/releases/))
- **Tomcat 9** 설치(서블릿 4.0 호환) (예: [Tomcat 9 다운로드](https://tomcat.apache.org/download-90.cgi))
- **Maven** 설치(권장) 또는 IDE(IntelliJ/Eclipse)로 빌드  
  - Maven: [Apache Maven 다운로드](https://maven.apache.org/download.cgi)

> Windows에서 `mvn`이 안 먹는다면, **환경변수 PATH**에 Maven의 `bin` 경로가 안 들어간 경우가 대부분입니다.

### 8-2. WAR 생성(Maven)

프로젝트 루트에서:

PowerShell/CMD에서(프로젝트 루트 기준):

```bat
cd WAS\tomcat-app
mvn -DskipTests package
```

성공하면 보통 아래에 WAR가 생성됩니다:

- `WAS/tomcat-app/target/neis-parents-service.war`

> 참고: 현재 PC에서 `mvn`이 인식되지 않으면, Maven을 설치하거나 IDE 빌드를 사용하세요.

### 8-3. Tomcat 배포

가장 쉬운 방법(로컬):

1. Tomcat 설치 폴더로 이동
2. `webapps/` 폴더에 WAR 파일 복사
   - 예) `.../apache-tomcat-9.0.xx/webapps/neis-parents-service.war`
3. Tomcat 실행
   - Windows: `bin/startup.bat`
4. 브라우저 접속
   - `http://localhost:8080/neis-parents-service/`

#### (강력 추천) 재배포할 때 “이것”을 꼭 하세요

Tomcat은 WAR를 풀어서 같은 이름의 폴더를 만들기 때문에, 재배포 시 아래를 해주면 꼬임이 줄어듭니다:

- `webapps/neis-parents-service.war` 교체
- **기존** `webapps/neis-parents-service/` 폴더 삭제(있다면)
- Tomcat 재시작

---

## 9) 자주 터지는 문제(인턴 체크리스트)

### 9-1. `/`로 들어갔는데 404
- Tomcat이 켜졌는지 확인
- 컨텍스트 경로가 `/neis-parents-service`인지 확인  
  (URL 끝에 `/neis-parents-service/` 붙여보기)
- `web.xml`의 welcome-file이 `index.html`인지 확인
- WAR 배포가 정상인지 `webapps/`에 폴더가 풀렸는지 확인

### 9-2. API 호출이 404
- URL이 `/api/attendance`인지 확인
- `WEB-INF/web.xml`에 해당 서블릿 매핑이 있는지 확인
- Tomcat 로그 확인(아래 참고)

### 9-3. DB 조회가 실패
- MariaDB가 실행 중인지 확인
- `NEIS_DB_URL/USER/PASSWORD` 설정 확인
- 테이블 존재 확인(`NEISDB.attendenceTB`)

---

## 10) 로그 보는 법(매우 중요)

Tomcat 오류는 거의 로그에 답이 있습니다.

- Tomcat 폴더: `logs/`
  - `catalina.out`(Linux) 또는 `catalina.YYYY-MM-DD.log`(Windows)

API가 500을 뱉거나, 서블릿이 로딩 안 되면 **로그를 먼저 확인**하세요.

---

## 11) 다음 단계(개발 흐름)

1) 페이지 추가(HTML)  
2) API 서블릿 추가(Java)  
3) `web.xml`에 매핑 추가  
4) DB 조회 쿼리 연결  
5) 프론트에서 `fetch()`로 연결  

이 흐름을 반복하면 됩니다.

---

## 12) (추가 팁) “우리 프로젝트에서” 가장 자주 쓰는 경로들

- **정적 페이지**
  - `index.html`, `login.html`, `register.html`, `find-account.html`
  - `student-attendance.html` (출결상황)
- **정적 리소스**
  - `css/style.css`, `js/main.js`, `images/*`
- **API**
  - `GET /api/attendance?studentId=...&from=YYYY-MM-DD&to=YYYY-MM-DD`


