# AGENTS.md

## Project

트립메이트는 여행 동행자들이 함께 여행 일정과 준비물을 관리하는 Android 앱이다.

## Working Language

사용자와의 대화, 문서, 화면 문구는 기본적으로 한국어를 사용한다.

코드 식별자와 파일명은 Android/Kotlin 관례에 따라 영어를 사용한다.

## Product Direction

- Android 앱을 우선 개발한다.
- 모바일 사용성을 가장 중요하게 본다.
- 여행 일정은 Day별, 시간별로 빠르게 확인할 수 있어야 한다.
- 동행자 모두가 일정과 준비물을 편집할 수 있다.
- 지도는 앱 내부에 표시하지 않고 외부 지도 앱을 실행한다.

## MVP Scope

포함:

- Google 로그인
- 여행 생성, 수정, 삭제
- 초대 코드 기반 여행 참여
- 여행 멤버 공동 편집
- Day별 일정 관리
- 전체 일정 보기
- 준비물 체크리스트
- 여행별 통화와 수동 환율
- 네이버지도 또는 구글지도 외부 실행

제외:

- 앱 내부 지도 표시
- 이동 시간 자동 계산
- 장소 검색 API
- AI 일정 추천
- PDF/이미지 내보내기
- 푸시 알림

## Recommended Stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Navigation
- Firebase Authentication
- Cloud Firestore
- Kotlin Coroutines

## Architecture

MVVM 구조를 기본으로 한다.

권장 패키지 방향:

- `ui`: Compose 화면과 컴포넌트
- `viewmodel`: 화면 상태와 사용자 액션 처리
- `domain`: 도메인 모델과 유스케이스
- `data`: Repository와 Firebase 연동
- `navigation`: 앱 내 화면 이동
- `map`: 외부 지도 앱 실행 처리

## Editing Guidelines

- 기존 문서와 설계를 우선 확인한다.
- 요구사항 변경이 있으면 `/docs` 문서를 먼저 갱신한다.
- 구현은 MVP 범위를 벗어나지 않도록 한다.
- UI는 밝고 깔끔한 여행 앱 느낌을 유지한다.
- 모바일 화면에서 텍스트와 버튼이 겹치지 않도록 확인한다.

## Important Docs

- `docs/01_overview.md`
- `docs/02_requirements.md`
- `docs/03_domain_model.md`
- `docs/04_screen_and_ui_spec.md`
- `docs/05_architecture.md`
- `docs/06_data_model.md`
- `docs/07_workflow.md`
- `docs/08_open_questions.md`
- `docs/09_implementation_plan.md`

