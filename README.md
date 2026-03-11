# 🍔 오더의 민족

> 광화문 근처 음식점들의 배달 주문 관리, 결제, 주문 내역 관리 기능을 제공하는 플랫폼

<br>

## 👥 팀원 소개

<table>
  <tbody>
    <tr>
      <td align="center"><b>최지원</b><br>팀장</td>
      <td align="center"><b>정성원</b><br>팀원</td>
      <td align="center"><b>차윤호</b><br>팀원</td>
      <td align="center"><b>김단비</b><br>팀원</td>
      <td align="center"><b>박선주</b><br>팀원</td>
      <td align="center"><b>남순식</b><br>팀원</td>
    </tr>
    <tr>
      <td align="center"><a href="https://github.com/ji-circle"><img src="https://github.com/ji-circle.png" width="100px;" alt="최지원"/></a></td>
      <td align="center"><a href="https://github.com/oharang"><img src="https://github.com/oharang.png" width="100px;" alt="정성원"/></a></td>
      <td align="center"><a href="https://github.com/ilovemusicandprogramming"><img src="https://github.com/ilovemusicandprogramming.png" width="100px;" alt="차윤호"/></a></td>
      <td align="center"><a href="https://github.com/danbeekimm"><img src="https://github.com/danbeekimm.png" width="100px;" alt="김단비"/></a></td>
      <td align="center"><a href="https://github.com/mimimya"><img src="https://github.com/mimimya.png" width="100px;" alt="박선주"/></a></td>
      <td align="center"><a href="https://github.com/sunsik17"><img src="https://github.com/sunsik17.png" width="100px;" alt="남순식"/></a></td>
    </tr>
    <tr>
      <td align="center"><a href="https://github.com/ji-circle"><sub><b>@ji-circle</b></sub></a></td>
      <td align="center"><a href="https://github.com/oharang"><sub><b>@oharang</b></sub></a></td>
      <td align="center"><a href="https://github.com/ilovemusicandprogramming"><sub><b>@ilovemusicandprogramming</b></sub></a></td>
      <td align="center"><a href="https://github.com/danbeekimm"><sub><b>@danbeekimm</b></sub></a></td>
      <td align="center"><a href="https://github.com/mimimya"><sub><b>@mimimya</b></sub></a></td>
      <td align="center"><a href="https://github.com/sunsik17"><sub><b>@sunsik17</b></sub></a></td>
    </tr>
  </tbody>
</table>

<br>

## 📋 역할 분담

| 이름 | 담당 기능 |
|------|-----------|
| 최지원 | 지역 · 배송지, 결제 |
| 정성원 | 메뉴 + 메뉴 이미지, 메뉴 설명 AI |
| 차윤호 | 카트, 주문 (Order & OrderItem), 결제 |
| 김단비 | 가게 + 가게 이미지 |
| 박선주 | 리뷰 + 리뷰 이미지, 평점 통계 |
| 남순식 | 유저, 카트 |

<br>

## ⚙️ 기술 스택

<div>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/QueryDSL-0096C7?style=for-the-badge&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Amazon_AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" />
</div>

<br>

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.5.11 (JDK 17) |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate, QueryDSL |
| Security | Spring Security, JWT |
| Cache | Redis |
| Build | Gradle |
| Infra | Amazon AWS |
| Version Control | Git / GitHub |

<br>

## 🚀 서비스 구성 및 실행 방법

```bash
# 1. 레포지토리 클론
git clone https://github.com/YOUR_REPO/order-nation.git
cd order-nation

# 2. 환경변수 설정 (.env 또는 application.yml)
# DB, Redis, JWT Secret, AWS, AI API Key 등 설정 필요

# 3. Docker로 PostgreSQL & Redis 실행 (선택)
docker-compose up -d

# 4. 프로젝트 빌드 및 실행
./gradlew clean build
./gradlew bootRun
```

> ⚠️ Java 17 이상, PostgreSQL, Redis가 사전에 설치되어 있어야 합니다.

## 🗺️ Architectuer
<img width="1062" height="600" alt="스크린샷 2026-03-04 오전 11 33 57" src="https://github.com/user-attachments/assets/1ac3f054-a60f-4955-ab51-3078f2368941" />



<br>

## 🗂️ ERD

> _(ERD 이미지를 여기에 첨부할 예정입니다)_

<br>

## 📌 프로젝트 핵심 기능

### 👤 사용자
<details>
  <summary>상세 기능 보기</summary>
- 회원가입 시 `customer` role 부여
- 사용자 정보는 `email`, `password`, `address`, `nickname`, `role`로 구성
- `email` 및 `nickname` 중복 불가
- 역할(Role) 체계: `customer` → `owner` 전환 신청 가능 (manager/master 승인 필요)


- 사용자는 자신의 정보를 조회 및 수정할 수 있습니다.
- `customer`는 가게 사장님(`owner`)으로 역할 변경을 신청할 수 있으며, `manager` 또는 `master`가 승인/거절합니다.

</details>

---

### 🏪 가게

<details>
<summary>상세 기능 보기</summary>
가게 상태: `PENDING`(승인 대기) → `CLOSED`(영업 종료) → `OPENED`(영업 중)

**사장님 `owner`**
- 가게 등록 요청 (이미지 1~10장 필수, 등록 후 PENDING 상태)
- 주소 중복 불가
- 가게 정보 수정 및 삭제 가능

**고객 `customer`**
- 배송지로부터 5km 이내 가게 목록 조회
- 특정 지역 또는 카테고리별 가게 조회 가능

**매니저 `manager`**
- PENDING 상태 가게 목록 조회
- 가게 등록 승인 (PENDING → CLOSED), 승인 시 `owner` role 자동 부여

</details>

---

### 🍽️ 상품 (메뉴)

<details>
<summary>상세 기능 보기</summary>

**고객 `customer`**
- 가게의 상품 조회 (사진 + 가격 + 설명)
- `숨김` 처리된 상품은 조회 불가

**사장님 `owner`**
- 상품 등록 / 수정 / 삭제
- 상품 상태: `활성` (기본값) / `숨김` / `품절`
- 상품당 이미지 최대 1장

**매니저 `manager`**
- 상품 상태 변경, 노출 여부 결정, 삭제 가능

</details>

---

### 🛒 장바구니

<details>
<summary>상세 기능 보기</summary>

- 한 가게의 메뉴만 담기 가능
- 다른 가게 메뉴 추가 시, 기존 장바구니 초기화 여부 확인
- 원하는 메뉴만 선택(체크박스)하여 주문 가능
- 주문 완료 후 장바구니 전체 삭제

</details>

---

### 📦 주문

<details>
<summary>상세 기능 보기</summary>
주문 상태: `PENDING` → `ACCEPTED` / `CANCELED` → `COMPLETED`

**고객 `customer`**
- 주문 생성 / 목록 조회 / 단건 조회
- `PENDING` 상태일 때만 배송지 · 요청사항 수정 및 취소 가능

**사장님 `owner`**
- 자신의 가게 주문 목록 조회 (상태별 필터링)
- 주문 수락 → `ACCEPTED` / 거절 → `CANCELED`

</details>

---

### 💳 결제

<details>
<summary>상세 기능 보기</summary>
  
결제 상태: `READY` → `SUCCESS` / `FAILED` / `CANCELED`

- 고객이 결제 방법 결정 → `READY` 상태 생성
- 결제 완료 → `SUCCESS`
- 결제 실패 → `FAILED`
- 고객 또는 사장님이 주문 취소 시 즉시 환불 → `CANCELED`

**조회 권한**
- `customer`: 자신의 결제 정보 단건 조회 (결제 수단, 총액, 시간)
- `manager` 이상: 결제 ID 또는 고객 ID로 결제 정보 조회

</details>

---

### ⭐ 리뷰

<details>
<summary>상세 기능 보기</summary>

**고객 `customer`**
- 주문 상태 `COMPLETED` 후, 주문일로부터 **2일 이내** 리뷰 작성 가능
- 리뷰 구성: 사진(최대 5장, 선택) + 평가(300자 이하, 선택) + 평점(1~5점)
- 평점 등록/삭제 시 가게 평균 평점에 자동 반영
- 작성일로부터 2일 이내 수정 가능, 삭제 가능

**사장님 `owner`**
- 자신의 가게를 제외한 가게에만 리뷰 작성 가능

**매니저 `manager`**
- 가게별 리뷰 목록/단건 조회, 단건 삭제

**마스터 `master`**
- 가게별 리뷰 목록/단건 조회, 수정, 삭제

</details>

---

### 🤖 AI (메뉴 설명 자동 생성)

- AI API 연동을 통해 상품(메뉴) 소개 문구를 자동으로 생성합니다.

<br>

## [📄 API 명세서](https://www.notion.so/teamsparta/API-3122dc3ef51480ba80b8e9effecd8463)
