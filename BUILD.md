# APK 빌드 가이드

이 문서는 `MlbparkBullpen` 프로젝트를 사용자 PC에서 디버그/릴리스 APK로
빌드하는 절차를 정리한 것입니다.

> **컨테이너 빌드가 불가능했던 이유**
> 본 어시스턴트의 작업 컨테이너는 Maven Central / npm 등 일반 패키지 저장소만
> 허용되어 있고, Android SDK 다운로드 서버(`dl.google.com`)와
> Gradle 배포 서버(`services.gradle.org`)가 차단되어 있어 APK를 직접 빌드할 수 없습니다.
> 따라서 아래 절차에 따라 **로컬 PC에서 빌드**해 주세요.

## 가장 쉬운 방법 — GitHub Actions (PC에 아무것도 설치하지 않음)

PC에 JDK / Android SDK / Gradle 을 전혀 설치하지 않고, GitHub 가 대신 빌드해서
APK 를 다운로드 가능한 파일로 올려줍니다.

1. GitHub 에서 새 저장소를 만듭니다 (private 도 OK).
2. 이 프로젝트 폴더 전체를 push 합니다:
   ```bash
   git init
   git add .
   git commit -m "init"
   git branch -M main
   git remote add origin https://github.com/<사용자명>/<저장소명>.git
   git push -u origin main
   ```
3. push 직후 GitHub 저장소의 **Actions** 탭에서 빌드가 자동 시작됩니다 (3~5 분).
4. 빌드 성공한 워크플로우 실행을 클릭 → 페이지 하단의 **Artifacts** 섹션에서
   `app-debug` 다운로드 → 압축 해제하면 `app-debug.apk` 가 나옵니다.
5. 이 APK 를 폰으로 옮겨 설치 (출처를 알 수 없는 앱 설치 허용 필요).

워크플로우 파일은 이미 `.github/workflows/build.yml` 에 포함되어 있어 별도
설정이 필요 없습니다. 코드를 수정해서 다시 push 할 때마다 새 APK 가 자동
생성됩니다.

---

## 방법 1. Android Studio (로컬 GUI 환경)

### 사전 준비
1. [Android Studio](https://developer.android.com/studio) 설치 (최신 안정 버전, Hedgehog 이상 권장)
2. Android Studio 실행 후 첫 시작 시 SDK 자동 설치 마법사 진행

### 프로젝트 열기
1. 전달받은 `MlbparkBullpen-source.zip` 압축 해제
2. Android Studio → **File → Open** → 압축 푼 `MlbparkBullpen` 폴더 선택
3. 첫 열림 시 Gradle 동기화가 자동 시작됨 (수 분 소요, 의존성 다운로드)
   - 만약 "JDK location" 경고가 뜨면: Settings → Build Tools → Gradle →
     **Gradle JDK** 를 `17` 또는 `21` 로 설정
   - `gradle-wrapper.jar` 가 없다고 하면: Terminal 탭에서 `gradle wrapper`
     실행 (Android Studio 내장 Gradle 사용)

### 디버그 APK 빌드 & 실행
- **실기기 사용**:
  1. 안드로이드 폰에서 개발자 모드 활성화 (설정 → 휴대전화 정보 → 빌드번호 7회 탭)
  2. USB 디버깅 ON, 케이블로 PC에 연결
  3. Android Studio 상단 디바이스 드롭다운에서 폰 선택 → ▶ Run 버튼
- **에뮬레이터 사용**:
  1. Tools → Device Manager → Create device → API 26 이상 이미지 선택
  2. 실행 후 ▶ Run

### 디버그 APK 파일 추출
- 메뉴: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- 빌드 완료 알림에서 **locate** 클릭하면 `app/build/outputs/apk/debug/app-debug.apk` 위치로 이동
- 이 APK 를 폰에 옮겨 사이드로딩 가능

### 릴리스(서명된) APK 빌드
1. 메뉴: **Build → Generate Signed Bundle / APK**
2. **APK** 선택 → Next
3. **Create new...** 로 keystore 생성
   - Key store path: 안전한 경로 (예: `~/keys/mlbpark.jks`)
   - 비밀번호와 alias 메모해 둘 것 (분실 시 같은 패키지로 업데이트 불가)
4. Next → **release** 빌드 변형 선택 → **Finish**
5. 결과: `app/release/app-release.apk`

---

## 방법 2. 커맨드라인 빌드 (CI 또는 헤드리스 환경)

### 사전 준비
필요한 도구:
- JDK 17 (필수, AGP 8.5와 호환)
- Android SDK Command-line Tools

### 1단계 — JDK 17 설치
- **macOS**: `brew install openjdk@17`
- **Ubuntu/Debian**: `sudo apt install openjdk-17-jdk`
- **Windows**: [Adoptium Temurin 17](https://adoptium.net/) 다운로드

설치 후 `JAVA_HOME` 설정:
```bash
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
java -version  # 17.x.x 확인
```

### 2단계 — Android SDK 설치
```bash
# 1. Command-line tools 다운로드 (https://developer.android.com/studio 하단 "Command line tools only" 섹션)
# Linux 예시:
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip
mv cmdline-tools latest

# 2. 환경 변수 설정
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# 3. 라이선스 동의 + 필수 패키지 설치
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 3단계 — Gradle Wrapper 준비
프로젝트에 `gradlew` 스크립트가 없다면 한 번 생성해야 합니다.

**A. 시스템에 Gradle이 이미 설치된 경우:**
```bash
cd MlbparkBullpen
gradle wrapper --gradle-version 8.7
```

**B. Gradle이 없으면** Android Studio로 한 번만 프로젝트를 열어 sync하면
자동으로 `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` 가 생성됩니다.

### 4단계 — 빌드
```bash
cd MlbparkBullpen
chmod +x gradlew  # macOS/Linux 한정

# 디버그 APK
./gradlew assembleDebug
# 결과: app/build/outputs/apk/debug/app-debug.apk

# 릴리스 APK (서명 미설정 → 설치는 가능하나 Play Store 업로드 불가)
./gradlew assembleRelease
# 결과: app/build/outputs/apk/release/app-release-unsigned.apk
```

### 5단계 — 폰에 설치
USB 디버깅으로 연결한 폰에 설치:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

또는 APK 파일을 카카오톡/이메일/구글드라이브로 폰에 보낸 뒤 직접 탭해 설치
(설정에서 "출처를 알 수 없는 앱 설치 허용" 필요).

---

## 릴리스 APK 서명 (커맨드라인)

서명되지 않은 APK는 폰에 직접 설치할 수 있는 일부 기기도 있지만, 일반적으로
서명된 APK 가 필요합니다.

### keystore 생성 (한 번만)
```bash
keytool -genkey -v \
  -keystore ~/keys/mlbpark.jks \
  -alias mlbpark \
  -keyalg RSA -keysize 2048 -validity 10000
```
- 비밀번호 입력 (storepass / keypass)
- 이름 / 조직 등 입력 (모두 "Unknown" 으로 둬도 됨)
- 생성된 `mlbpark.jks` 파일은 **절대 분실하지 말 것** (분실 시 같은 패키지명으로 업데이트 불가능)

### app/build.gradle.kts 에 signingConfig 추가
`android { ... }` 블록 안에 추가:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("MLBPARK_KEYSTORE") ?: "../keys/mlbpark.jks")
        storePassword = System.getenv("MLBPARK_STORE_PASSWORD")
        keyAlias = "mlbpark"
        keyPassword = System.getenv("MLBPARK_KEY_PASSWORD")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ...기존 isMinifyEnabled / proguardFiles 유지
    }
}
```

### 환경변수 설정 후 빌드
```bash
export MLBPARK_KEYSTORE=~/keys/mlbpark.jks
export MLBPARK_STORE_PASSWORD=비밀번호
export MLBPARK_KEY_PASSWORD=비밀번호
./gradlew assembleRelease
# 결과: app/build/outputs/apk/release/app-release.apk  (서명 완료)
```

---

## 빌드 후 첫 실행 체크리스트

앱을 처음 실행했을 때 점검할 항목:

1. **목록이 비어 있다** → mlbpark 가 selectors 를 변경했거나 광고가 끼었을 수 있음.
   `BullpenRepository.fetchList()` 의 selectors 검토:
   - `a.txt`, `a.list_word`, `span.nick`, `span.date`
   - PRD 에 명시된 그대로지만 실제 HTML 에 클래스명이 다를 수 있음.

2. **상세 페이지 본문이 비어 있다** → `fetchDetail()` 의 본문 컨테이너 후보 리스트
   조정 필요. 실제 HTML 을 폰의 Chrome 에서 같은 URL 열어서 확인:
   ```kotlin
   val bodyEl: Element? = listOf(
       "#contents",
       ".article_word",
       ".view_content",
       ".bbs_view",
       "#user_contents",
   ).firstNotNullOfOrNull { sel -> doc.selectFirst(sel) }
   ```
   여기에 실제 컨테이너의 ID/클래스를 추가.

3. **이미지가 깨진다** → `extractBodyBlocks()` 안에서 `<img>` 의 `src` 가
   `data-original` 같은 lazy-load 속성에 들어 있을 가능성. 이미 fallback 처리는
   되어 있지만, 사이트가 다른 속성명을 쓰면 추가 필요.

4. **인코딩이 깨진다** → 사이트가 EUC-KR 인 경우 `Jsoup.parse` 두 번째 인자에
   `"EUC-KR"` 명시 필요. 현재는 charset 자동 감지 (null 전달).

---

## 향후 배포 옵션

| 채널 | 필요한 것 | 난이도 |
|------|----------|--------|
| 카카오톡/메일로 APK 직접 전송 | 서명된 APK | ★ |
| GitHub Releases | 서명된 APK + 릴리스 페이지 | ★★ |
| F-Droid (오픈소스 한정) | 소스 공개 + 메타데이터 PR | ★★★ |
| Google Play | 키스토어 + Play Console + 정책 검수 | ★★★★ |

비공식 앱 (제3자 사이트 스크레이핑) 은 Google Play 정책상 거절될 가능성이
있으므로, 개인 사용 또는 GitHub Releases 사이드로딩 배포가 가장 현실적입니다.
