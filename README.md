# Mlbpark Bullpen Viewer

MLB Park Bullpen 게시판(https://mlbpark.donga.com/mp/b.php?b=bullpen)을 모바일에서
편하게 보기 위한 Android 앱.

## 기능 (MVP)
- 첫 화면: 최신 글 10개 (제목 / 카테고리 / 작성자 / 작성시간)
- 글 선택 시 상세 화면으로 이동: 제목, 카테고리, 작성자, 작성시간, 본문
- Pull-to-Refresh 로 목록 갱신
- 시안 A (Stadium / Dark) 디자인

## 기술 스택
- Kotlin 2.0 + Jetpack Compose (Material3)
- OkHttp + Jsoup (HTML 파싱)
- Coil (이미지 로딩)
- Navigation Compose
- minSdk 26 / targetSdk 34

## 프로젝트 구조

```
app/src/main/java/com/mlbpark/bullpen/
├── MainActivity.kt              # 진입점 + Navigation host
├── data/
│   ├── Models.kt                # PostSummary, PostDetail, BodyBlock
│   └── BullpenRepository.kt     # HTTP 호출 + Jsoup 파싱
└── ui/
    ├── UiState.kt               # Loading / Success / Error
    ├── ListViewModel.kt
    ├── DetailViewModel.kt
    ├── CategoryChip.kt
    ├── theme/
    │   ├── Color.kt             # 시안 A 색상 토큰
    │   └── Theme.kt
    └── screen/
        ├── ListScreen.kt
        └── DetailScreen.kt
```

## 빌드 (BUILD.md 참고)
세 가지 방법 중 편한 것을 선택:
- **GitHub Actions** — PC에 아무것도 설치 안 함, push 만 하면 APK 자동 생성 (가장 쉬움)
- **Android Studio** — 폴더 열고 ▶ Run
- **CLI** — `./gradlew assembleDebug`

자세한 절차는 `BUILD.md` 를 참조.

## 파싱 셀렉터 (PRD에 명시된 그대로)
- 글 제목: `a.txt`
- 카테고리: `a.list_word`
- 작성자: `span.nick`
- 작성시간: `span.date`

같은 게시글 행에 4개가 묶여 있다는 가정으로, `a.txt` 의 가장 가까운
`<tr>` 조상 안에서 나머지 3개를 찾는 방식이라 광고/공지 행이 자동으로 걸러진다.
