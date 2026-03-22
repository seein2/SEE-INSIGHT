# AGENTS.md - SEE-INSIGHT

> AI 에이전트를 위한 프로젝트 가이드. 코드 수정 전 반드시 숙지할 것.

## 1. 프로젝트 개요

Perplexity API를 활용한 뉴스 자동 요약 및 이메일 발송 서비스.

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.9
- **Build**: Gradle
- **Database**: MySQL 8.0 (Docker), H2 (테스트)
- **Cache**: Redis (Docker)
- **ORM**: JPA (Hibernate)
- **API Docs**: SpringDoc (Swagger-UI) → `/swagger-ui/index.html`
- **Security**: Spring Security + JWT (Refresh Token: Redis)
- **Testing**: JUnit5, Mockito, AssertJ

---

## 2. 빌드/테스트 명령어

```bash
# 전체 빌드
./gradlew build

# 전체 테스트
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "MemberServiceTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "MemberServiceTest.getMember_success"

# 패키지 내 테스트 실행
./gradlew test --tests "com.seein.domain.member.*"

# 컴파일만 (테스트 제외)
./gradlew compileJava compileTestJava

# 클린 빌드
./gradlew clean build

# 앱 실행
./gradlew bootRun

# 로컬 인프라 (MySQL, Redis)
docker-compose up -d
```

---

## 3. 패키지 구조

```
com.seein
├── global/                    # 공통 모듈
│   ├── config/               # 설정 클래스
│   ├── dto/                  # GlobalResponseDto
│   ├── entity/               # BaseTimeEntity
│   ├── exception/            # ErrorCode, BusinessException, GlobalExceptionHandler
│   └── security/             # JWT, OAuth2 설정
│
├── domain/
│   ├── auth/                 # 인증/인가 (OAuth2, JWT)
│   ├── member/               # 회원 관리
│   ├── keyword/              # 키워드 및 구독
│   ├── news/                 # 뉴스 데이터 및 요약
│   └── dashboard/            # 대시보드
```

---

## 4. 코딩 컨벤션

### 4.1 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `MemberService`, `SubscriptionController` |
| 변수/메서드 | camelCase | `getMember()`, `subscriptionId` |
| 상수 | UPPER_SNAKE | `MAX_RETRY_COUNT` |
| DB 테이블/컬럼 | snake_case | `member_id`, `created_at` |

### 4.2 Lombok 사용 규칙

```java
// ✅ 허용
@Getter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// ❌ 금지
@Data  // 절대 사용 금지
@Setter  // Entity에서 사용 금지 (update 메서드로 대체)
```

### 4.3 의존성 주입 (DI)

```java
// ✅ 올바른 방식: 생성자 주입 + @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;  // 반드시 private final
}

// ❌ 금지: 필드 주입
@Autowired
private MemberRepository memberRepository;
```

### 4.4 API 응답 형식

```java
// 모든 성공 응답: GlobalResponseDto<T>
@GetMapping("/{id}")
public GlobalResponseDto<MemberResponse> getMember(@PathVariable Integer id) {
    return GlobalResponseDto.success(memberService.getMember(id));
}

// 에러 응답: GlobalExceptionHandler가 자동 처리
throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
```

### 4.5 예외 처리

```java
// 비즈니스 예외는 BusinessException + ErrorCode 사용
public Member findById(Integer id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
}

// 새 에러 코드 추가 시 ErrorCode enum에 정의
SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다."),
```

### 4.6 Entity 작성 규칙

```java
@Entity
@Table(name = "member")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용 protected 기본 생성자
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer memberId;

    // Setter 대신 명시적 update 메서드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 정적 팩토리 메서드 권장
    public static Member create(String email, String nickname, String provider) {
        Member member = new Member();
        member.email = email;
        // ...
        return member;
    }
}
```

---

## 5. 테스트 작성 규칙

### 5.1 단위 테스트 (Service)

```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 정보 조회 성공")  // 한글 DisplayName 필수
    void getMember_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        given(memberRepository.findById(1)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMember(1);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }
}
```

### 5.2 컨트롤러 테스트

```java
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    @WithMockUser  // Spring Security 인증 우회
    @DisplayName("회원 조회 API 테스트")
    void getMember_api() throws Exception {
        mockMvc.perform(get("/api/v1/members/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
}
```

---

## 6. 주석 규칙

```java
/**
 * 회원 서비스
 * 회원 CRUD 및 인증 관련 비즈니스 로직 처리
 */
@Service
public class MemberService {

    /**
     * 회원 정보 조회
     * @param memberId 회원 ID
     * @return 회원 응답 DTO
     * @throws BusinessException 회원이 존재하지 않을 경우
     */
    public MemberResponse getMember(Integer memberId) {
        // 복잡한 로직에는 인라인 한글 주석
    }
}
```

---

## 7. 금지 사항

- `@Data` 사용 금지
- Entity에 `@Setter` 사용 금지
- 필드 주입 (`@Autowired` on field) 금지
- `as any`, `@SuppressWarnings` 남용 금지
- 빈 catch 블록 `catch(e) {}` 금지
- 하드코딩된 비밀값 (API 키 등) 커밋 금지

---

## 8. 파일/폴더 무시 목록

커밋하면 안 되는 파일:
- `.env` - 환경 변수
- `mysql_data/` - 로컬 DB 데이터
- `node_modules/` - 프론트엔드 의존성
- `.sisyphus/` - AI 에이전트 작업 파일

---

## 9. API 엔드포인트 패턴

```
GET    /api/v1/{resource}           # 목록 조회
GET    /api/v1/{resource}/{id}      # 상세 조회
POST   /api/v1/{resource}           # 생성
PATCH  /api/v1/{resource}/{id}      # 부분 수정
DELETE /api/v1/{resource}/{id}      # 삭제
```

---

## 10. 커밋 메시지 컨벤션(한글)

```
feat: 새 기능 추가
fix: 버그 수정
refactor: 리팩토링
test: 테스트 추가/수정
docs: 문서 수정
chore: 빌드, 설정 변경
```

예시: `feat(member): 회원 탈퇴 기능 추가`
