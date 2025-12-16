# NEIS Parents Service (WAS)

이 저장소는 **NEIS 학부모서비스의 WAS(Tomcat) 프로젝트**만 포함합니다.

- 정적 페이지: `src/main/webapp/*.html`
- 공통 스타일/스크립트: `src/main/webapp/css`, `src/main/webapp/js`
- Java API(서블릿): `src/main/java/com/neis/servlet`
- 배포/운영 가이드: `배포_가이드_Tomcat.md`

## 빠른 시작(개발/테스트)

1. Tomcat 9에 WAR로 배포하거나, Tomcat의 `webapps/`에 WAR를 복사합니다.
2. 접속: `http://localhost:8080/neis-parents-service/`

자세한 내용은 `배포_가이드_Tomcat.md`를 참고하세요.

