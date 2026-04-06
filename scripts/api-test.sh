#!/usr/bin/env bash
# =============================================================================
# API Integration Test — 회원 서비스 전체 엔드포인트 검증
# 사용법: ./scripts/api-test.sh [BASE_URL]
# 기본값: http://localhost:8080
# 요구사항: httpie, jq
# =============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0

# ─── 출력 헬퍼 ────────────────────────────────────────────────────────────────

green()  { echo -e "\033[32m✔ $*\033[0m"; }
red()    { echo -e "\033[31m✘ $*\033[0m"; }
blue()   { echo -e "\033[34m▶ $*\033[0m"; }
yellow() { echo -e "\033[33m  $*\033[0m"; }
divider(){ echo "────────────────────────────────────────────────────────"; }

assert_status() {
    local label="$1" expected="$2" actual="$3"
    if [ "$actual" -eq "$expected" ]; then
        green "$label (HTTP $actual)"
        PASS=$((PASS + 1))
    else
        red "$label — 기대: HTTP $expected, 실제: HTTP $actual"
        FAIL=$((FAIL + 1))
    fi
}

# HTTPie 래퍼: 상태 코드만 추출
status() {
    http --check-status --ignore-stdin "$@" 2>/dev/null \
        | head -1 | awk '{print $2}' || \
    http --ignore-stdin "$@" 2>&1 \
        | grep "^HTTP/" | awk '{print $2}'
}

# HTTPie 래퍼: 응답 바디(JSON)를 stdout으로
body() {
    http --ignore-stdin --print=b "$@" 2>/dev/null
}

echo ""
echo "============================================================"
echo "  회원 서비스 API Integration Test"
echo "  BASE_URL: $BASE_URL"
echo "============================================================"

# =============================================================================
# 1. 회원 가입
# =============================================================================
divider
blue "1. 회원 가입"

USER_EMAIL="testuser_$(date +%s)@example.com"
USER_PW="Password1!"
USER_NAME="홍길동"
USER_PHONE="0101234$(( RANDOM % 9000 + 1000 ))"

SIGNUP_STATUS=$(http --ignore-stdin --print=h POST "$BASE_URL/api/v1/auth/signup" \
    Content-Type:application/json \
    email="$USER_EMAIL" password="$USER_PW" name="$USER_NAME" phoneNumber="$USER_PHONE" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')

assert_status "POST /api/v1/auth/signup" 202 "$SIGNUP_STATUS"
yellow "email=$USER_EMAIL / phone=$USER_PHONE"

# 중복 가입 시 — Consumer가 처리 후 중복 거부 (큐 발행은 202)
SIGNUP_DUP_STATUS=$(http --ignore-stdin --print=h POST "$BASE_URL/api/v1/auth/signup" \
    Content-Type:application/json \
    email="$USER_EMAIL" password="$USER_PW" name="$USER_NAME" phoneNumber="$USER_PHONE" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "POST /api/v1/auth/signup (중복 이메일도 202 — Consumer가 중복 처리)" 202 "$SIGNUP_DUP_STATUS"

# =============================================================================
# 2. 로그인 전 준비 — Consumer 처리 대기 (최대 10초 재시도)
# =============================================================================
divider
blue "2. Consumer 처리 대기 (RabbitMQ 비동기)"

LOGIN_RESP=""
LOGIN_STATUS=""
MAX_RETRY=5
RETRY_INTERVAL=2

for i in $(seq 1 $MAX_RETRY); do
    yellow "로그인 시도 ${i}/${MAX_RETRY} (${RETRY_INTERVAL}초 대기 후)..."
    sleep "$RETRY_INTERVAL"
    LOGIN_RESP=$(body POST "$BASE_URL/api/v1/auth/login" \
        Content-Type:application/json \
        email="$USER_EMAIL" password="$USER_PW")
    LOGIN_STATUS=$(echo "$LOGIN_RESP" | jq -r 'if .accessToken then "200" else "401" end' 2>/dev/null || echo "401")
    if [ "$LOGIN_STATUS" = "200" ]; then
        break
    fi
done

# =============================================================================
# 3. 로그인
# =============================================================================
divider
blue "3. 로그인"

assert_status "POST /api/v1/auth/login" 200 "$LOGIN_STATUS"

if [ "$LOGIN_STATUS" != "200" ]; then
    red "로그인 실패 — Consumer가 메시지를 처리하지 못했거나 서버 오류입니다."
    red "서버 로그를 확인하세요. 이후 테스트를 건너뜁니다."
    echo ""
    echo "============================================================"
    echo "  테스트 결과"
    echo "  통과: ${PASS} / $((PASS + FAIL))"
    red "  실패: ${FAIL}건"
    echo "============================================================"
    exit 1
fi

ACCESS_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.accessToken')
REFRESH_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.refreshToken')
yellow "accessToken=${ACCESS_TOKEN:0:30}..."
yellow "refreshToken=${REFRESH_TOKEN:0:30}..."

# 잘못된 비밀번호
WRONG_PW_RESP=$(body POST "$BASE_URL/api/v1/auth/login" \
    Content-Type:application/json \
    email="$USER_EMAIL" password="WrongPassword1!" 2>/dev/null || echo '{}')
WRONG_PW_STATUS=$(echo "$WRONG_PW_RESP" | jq -r 'if .accessToken then "200" else "401" end' 2>/dev/null || echo "401")
assert_status "POST /api/v1/auth/login (잘못된 비밀번호 → 401)" 401 "$WRONG_PW_STATUS"

# =============================================================================
# 4. 회원 정보 조회
# =============================================================================
divider
blue "4. 본인 정보 조회"

ME_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/members/me" \
    "Authorization:Bearer $ACCESS_TOKEN" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "GET /api/v1/members/me" 200 "$ME_STATUS"

# 토큰 없이 접근 → 401
UNAUTH_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/members/me" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "GET /api/v1/members/me (토큰 없음 → 401)" 401 "$UNAUTH_STATUS"

# =============================================================================
# 5. 회원 정보 수정
# =============================================================================
divider
blue "5. 본인 정보 수정"

UPDATE_STATUS=$(http --ignore-stdin --print=h PUT "$BASE_URL/api/v1/members/me" \
    "Authorization:Bearer $ACCESS_TOKEN" \
    Content-Type:application/json \
    name="홍길순" phoneNumber="01098765432" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "PUT /api/v1/members/me" 204 "$UPDATE_STATUS"

ME_AFTER=$(body GET "$BASE_URL/api/v1/members/me" "Authorization:Bearer $ACCESS_TOKEN")
yellow "수정 후 이름: $(echo "$ME_AFTER" | jq -r '.name')"

# =============================================================================
# 6. 토큰 재발급
# =============================================================================
divider
blue "6. Access Token 재발급"

# 단일 호출: 헤더+바디 동시 수신 (refresh token rotation으로 2회 호출 불가)
REFRESH_RESULT=$(http --ignore-stdin --print=hb POST "$BASE_URL/api/v1/auth/refresh" \
    Content-Type:application/json \
    refreshToken="$REFRESH_TOKEN" 2>/dev/null || true)

REFRESH_STATUS=$(echo "$REFRESH_RESULT" | head -1 | awk '{print $2}')
assert_status "POST /api/v1/auth/refresh" 200 "$REFRESH_STATUS"

NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESULT" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4 || true)
yellow "새 accessToken=${NEW_ACCESS_TOKEN:0:30}..."

# =============================================================================
# 7. 관리자 API — USER 권한으로 접근 시 403
# =============================================================================
divider
blue "7. 관리자 API 인가 검증"

ADMIN_FORBIDDEN=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/admin/members" \
    "Authorization:Bearer ${NEW_ACCESS_TOKEN:-$ACCESS_TOKEN}" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}' || true)
assert_status "GET /api/v1/admin/members (USER 권한 → 403)" 403 "$ADMIN_FORBIDDEN"

# =============================================================================
# 8. 관리자 로그인 (별도 ADMIN 계정 필요)
# =============================================================================
divider
blue "8. 관리자 로그인"
yellow "사전 조건: DB에 ROLE=ADMIN 계정이 있어야 합니다."
yellow "없으면 이 섹션의 테스트는 건너뜁니다."

ADMIN_EMAIL="${ADMIN_EMAIL:-admin@example.com}"
ADMIN_PW="${ADMIN_PW:-AdminPassword1!}"

ADMIN_LOGIN_RESP=$(body POST "$BASE_URL/api/v1/auth/login" \
    Content-Type:application/json \
    email="$ADMIN_EMAIL" password="$ADMIN_PW" 2>/dev/null || echo '{}')

ADMIN_ACCESS_TOKEN=$(echo "$ADMIN_LOGIN_RESP" | jq -r '.accessToken // empty')

if [ -n "$ADMIN_ACCESS_TOKEN" ] && [ "$ADMIN_ACCESS_TOKEN" != "null" ]; then
    ADMIN_LOGIN_STATUS=200
    assert_status "POST /api/v1/auth/login (ADMIN)" 200 "$ADMIN_LOGIN_STATUS"
    yellow "adminToken=${ADMIN_ACCESS_TOKEN:0:30}..."

    # 회원 목록 조회
    MEMBER_LIST_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/admin/members" \
        "Authorization:Bearer $ADMIN_ACCESS_TOKEN" \
        2>/dev/null | grep "^HTTP" | awk '{print $2}')
    assert_status "GET /api/v1/admin/members" 200 "$MEMBER_LIST_STATUS"

    # 이름 필터
    FILTERED_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/admin/members" \
        "Authorization:Bearer $ADMIN_ACCESS_TOKEN" \
        name=="홍" \
        2>/dev/null | grep "^HTTP" | awk '{print $2}')
    assert_status "GET /api/v1/admin/members?name=홍" 200 "$FILTERED_STATUS"

    # 특정 회원 상세 조회
    ME_ID=$(body GET "$BASE_URL/api/v1/members/me" "Authorization:Bearer $ACCESS_TOKEN" | jq -r '.id')
    if [ -n "$ME_ID" ] && [ "$ME_ID" != "null" ]; then
        DETAIL_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/admin/members/$ME_ID" \
            "Authorization:Bearer $ADMIN_ACCESS_TOKEN" \
            2>/dev/null | grep "^HTTP" | awk '{print $2}')
        assert_status "GET /api/v1/admin/members/$ME_ID" 200 "$DETAIL_STATUS"

        # 존재하지 않는 회원 조회 → 404
        NOT_FOUND_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/admin/members/99999" \
            "Authorization:Bearer $ADMIN_ACCESS_TOKEN" \
            2>/dev/null | grep "^HTTP" | awk '{print $2}')
        assert_status "GET /api/v1/admin/members/99999 (없는 회원 → 404)" 404 "$NOT_FOUND_STATUS"
    fi
else
    yellow "ADMIN 계정 없음 — 관리자 API 테스트 건너뜀"
    yellow "실행 방법: ADMIN_EMAIL=admin@example.com ADMIN_PW=AdminPassword1! ./scripts/api-test.sh"
fi

# =============================================================================
# 9. 로그아웃
# =============================================================================
divider
blue "9. 로그아웃"

LOGOUT_STATUS=$(http --ignore-stdin --print=h POST "$BASE_URL/api/v1/auth/logout" \
    "Authorization:Bearer $ACCESS_TOKEN" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "POST /api/v1/auth/logout" 204 "$LOGOUT_STATUS"

# 로그아웃 후 블랙리스트된 토큰으로 접근 → 401
BLACKLIST_STATUS=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/members/me" \
    "Authorization:Bearer $ACCESS_TOKEN" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "GET /api/v1/members/me (로그아웃 후 → 401)" 401 "$BLACKLIST_STATUS"

# =============================================================================
# 10. 회원 탈퇴 (새 토큰으로 재로그인 후)
# =============================================================================
divider
blue "10. 회원 탈퇴"

RE_LOGIN=$(body POST "$BASE_URL/api/v1/auth/login" \
    Content-Type:application/json \
    email="$USER_EMAIL" password="$USER_PW")
NEW_TOKEN=$(echo "$RE_LOGIN" | jq -r '.accessToken')

WITHDRAW_STATUS=$(http --ignore-stdin --print=h DELETE "$BASE_URL/api/v1/members/me" \
    "Authorization:Bearer $NEW_TOKEN" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "DELETE /api/v1/members/me" 204 "$WITHDRAW_STATUS"

# 탈퇴 후 접근 → 401 (토큰 무효화)
AFTER_WITHDRAW=$(http --ignore-stdin --print=h GET "$BASE_URL/api/v1/members/me" \
    "Authorization:Bearer $NEW_TOKEN" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}')
assert_status "GET /api/v1/members/me (탈퇴 후 → 401)" 401 "$AFTER_WITHDRAW"

# 탈퇴 후 재로그인 시도 → 401
RELOGIN_AFTER_WITHDRAW=$(http --ignore-stdin --print=h POST "$BASE_URL/api/v1/auth/login" \
    Content-Type:application/json \
    email="$USER_EMAIL" password="$USER_PW" \
    2>/dev/null | grep "^HTTP" | awk '{print $2}' || true)
assert_status "POST /api/v1/auth/login (탈퇴 후 재로그인 → 401)" 401 "$RELOGIN_AFTER_WITHDRAW"

# =============================================================================
# 결과 요약
# =============================================================================
divider
TOTAL=$((PASS + FAIL))
echo ""
echo "============================================================"
echo "  테스트 결과"
echo "  통과: $PASS / $TOTAL"
if [ "$FAIL" -gt 0 ]; then
    red "  실패: ${FAIL}건"
else
    green "  모든 테스트 통과"
fi
echo "============================================================"
echo ""

[ "$FAIL" -eq 0 ]
