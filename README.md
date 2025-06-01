# FindRoom API Backend

## 개요

강의실 예약/관리 시스템의 REST API 백엔드

## API 엔드포인트

### 🔐 인증

#### 회원가입

```http
POST http://localhost:8080/api/register
Content-Type: application/json

{
  "userId": "test",
  "name": "gildong",
  "password": "test",
  "user_type": "undergraduate"
}
```

#### 로그인

```http
POST http://localhost:8080/api/login
Content-Type: application/json

{
  "userId": "test",
  "password": "test",
  "user_type": "undergraduate"
}
```

#### 로그아웃

```http
POST http://localhost:8080/api/logout
```

#### 로그인 상태 확인

```http
GET http://localhost:8080/api/auth/me
```

**응답 예시:**

```json
{
  "success": true,
  "message": "로그인 상태 확인 완료",
  "data": {
    "loggedIn": true,
    "userId": "test"
  }
}
```

### 🏫 강의실

#### 강의실 검색

```http
GET http://localhost:8080/api/lectureroom/search?building=창조관&weekday=월&time=14:00:00

{
  "success": true,
  "message": "강의실 검색 완료",
  "data": {
    "availableRooms": [
      "cha102",
      "cha141",
      "cha156",
      "cha157",
      "cha511"
    ],
    "count": 5
  }
}
```

#### 강의실 상세조회

```http
GET http://localhost:8080/api/lectureroom/select?building=창조관&classId=cha511&weekday=월

{
  "success": true,
  "message": "강의실 조회 완료",
  "data": {
    "classId": "cha511",
    "room": "511",
    "building": "창조관",
    "floor": 5,
    "capacity": 60,
    "currentOccupancy": 0,
    "startTime": "17:00:00",
    "endTime": "19:00:00",
    "top3Hashtags": []
  }
}
```

### ⭐ 즐겨찾기

#### 자동 추가 (최근 히스토리 기반)

```http
PUT http://localhost:8080/api/favorites/add
```

#### 수동 추가

```http
POST http://localhost:8080/api/favorites/add-manual

{
  "classId": "cha511",
  "weekday": "월",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "participantCount": 2
}
```

#### 즐겨찾기 삭제

```http
POST http://localhost:8080/api/favorites/del

{
  "classId": "cha511",
  "weekday": "월",
  "startTime": "14:00:00",
  "endTime": "16:00:00"
}
```

### 📊 기타

#### 마이페이지 조회

```http
POST http://localhost:8080/api/mypage/info
```

## ⚠️ 주의사항

- 모든 API는 Cookie 기반 인증 사용
- 로그인 후 Cookie가 자동으로 설정됨
- @Valid 검증 적용으로 잘못된 데이터 시 400 에러 반환
- **history/add -> usage/start 순서로 사용되어야 함**
