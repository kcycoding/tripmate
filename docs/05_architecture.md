# 아키텍처

## 추천 기술 스택

- Android Native
- Kotlin
- Jetpack Compose
- Firebase Authentication
- Cloud Firestore
- Kotlin Coroutines
- AndroidX Navigation
- Material 3

## 구조

앱은 MVVM 구조를 기준으로 한다.

레이어:

- UI: Jetpack Compose 화면과 컴포넌트
- ViewModel: 화면 상태와 사용자 액션 처리
- Repository: Firebase와 도메인 모델 사이의 데이터 접근
- Data Source: Firebase Authentication, Firestore, 외부 지도 Intent

## 인증

Firebase Authentication의 Google Sign-In을 사용한다.

로그인 성공 후 Firebase UID를 기준으로 사용자 문서를 생성하거나 갱신한다.

## 데이터 저장

Cloud Firestore를 사용한다.

주요 컬렉션:

- `users`
- `trips`
- `trips/{tripId}/members`
- `trips/{tripId}/scheduleItems`
- `trips/{tripId}/packingItems`

## 동기화

Firestore 실시간 리스너를 사용하면 멤버가 변경한 일정과 준비물이 자동으로 반영된다.

1차 MVP에서는 실시간 리스너를 다음 화면에 우선 적용한다.

- 여행 목록
- Day별 일정
- 전체 일정
- 준비물

## 권한

Firestore 보안 규칙으로 다음을 제한한다.

- 로그인한 사용자만 데이터 접근 가능
- 여행 멤버만 해당 여행 데이터 읽기 가능
- 여행 멤버만 일정과 준비물 쓰기 가능
- 여행 owner만 여행 삭제, 멤버 제거, 초대 코드 재생성 가능

## 외부 지도 실행

앱 내부 지도 SDK는 사용하지 않는다.

지도 실행은 Android Intent 또는 URL Scheme으로 처리한다.

기본 정책:

- 여행 설정의 `mapProvider` 값을 확인한다.
- `naver`이면 네이버지도 실행을 시도한다.
- `google`이면 구글지도 실행을 시도한다.
- 설치되지 않은 경우 웹 지도 또는 Play Store 안내로 대체한다.

## 오프라인

Firestore의 로컬 캐시를 활용할 수 있다. 다만 1차 MVP에서는 오프라인 편집 충돌 해결을 별도 기능으로 만들지 않는다.

## 향후 확장

- 장소 검색 API 연동
- 예산 합계
- PDF/이미지 내보내기
- 푸시 알림
- AI 여행 일정 추천

