# FindRoom API Backend

## 개요

강의실 예약/관리 시스템의 REST API 백엔드

## API 엔드포인트

### 🔐 인증

- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃

### 🏫 강의실

- `GET /api/lectureroom/search` - 강의실 검색
- `GET /api/lectureroom/select` - 강의실 상세조회

### 📝 사용 기록

- `PUT /api/usage/start` - 사용 시작
- `PUT /api/usage/end` - 사용 종료

### ⭐ 즐겨찾기

- `PUT /api/favorites/add` - 자동 추가
- `POST /api/favorites/add-manual` - 수동 추가
- `DELETE /api/favorites/del` - 삭제
- `POST /api/favorites/list` - 목록 조회

### 📊 기타

- `POST /api/history/add` - 히스토리 추가
- `POST /api/mypage/info` - 마이페이지 조회

## 설정

1. `application-example.properties`를 `application.properties`로 복사
2. DB 연결 정보 수정
3. `./gradlew bootRun` 실행

## 주의사항

- 모든 API는 Cookie 기반 인증 사용
- @Valid 검증 적용됨
- CORS 설정 필요 시 추가 설정
