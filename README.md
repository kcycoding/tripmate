# TripMate

트립메이트는 여행 동행자들이 함께 일정과 준비물을 관리하는 Android 앱입니다.

## 현재 상태

- Android 패키지명: `com.tripmate.app`
- UI: Kotlin + Jetpack Compose
- 백엔드: Firebase Authentication + Cloud Firestore
- 지도: 앱 내부 지도 없이 외부 지도 앱 실행 예정

## 개발 준비

1. Firebase Console에서 Android 앱을 등록합니다.
2. `google-services.json`을 `app/google-services.json`에 배치합니다.
3. Android Studio에서 프로젝트를 엽니다.
4. Gradle Sync를 실행합니다.
5. `signingReport`로 SHA-1/SHA-256을 확인하고 Firebase에 등록합니다.

