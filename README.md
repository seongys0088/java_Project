# 🎬 CinePrime: 시네마 관리 시스템 (v1.0)

## 🌟 프로젝트 개요

CinePrime은 영화 예매, 매점 주문, 상영 스케줄 및 사용자 관리를 위한 통합 데스크톱 애플리케이션입니다. 사용자(고객)는 영화 정보를 조회하고, 좌석을 선택하여 예매 및 취소하며, 매점 상품을 주문할 수 있습니다. 관리자는 영화, 상영관, 스케줄, 매점 메뉴를 관리하고 매출을 분석할 수 있는 기능을 제공합니다.

## ✨ 주요 기능

### 1. 사용자 모드 (고객)
| 기능 | 설명 | 관련 파일 |
| :--- | :--- | :--- |
| **영화 예매** | 날짜별 상영 시간표를 조회하고, 실시간 예약 현황을 반영하여 좌석을 선택하고 결제합니다. | `UserMainPanel.java`, `SeatSelectionDialog.java`, `ReservationDAO.java` |
| **예매 순위** | 현재 예매율에 따른 영화 순위를 조회합니다. 검색 기능이 포함되어 있습니다. | `RankingPanel.java` |
| **마이 페이지** | 예매 내역을 확인하고 상영 전인 영화에 대해 예매 취소를 수행합니다. | `MyPagePanel.java` |
| **스낵바 주문** | 팝콘, 음료 등 매점 메뉴를 장바구니에 담아 주문하고 주문 내역을 조회합니다. | `SnackShopPanel.java`, `SnackDAO.java` |

### 2. 관리자 모드
| 기능 | 설명 | 관련 파일 |
| :--- | :--- | :--- |
| **대시보드** | 오늘 총 매출, 방문자 수, 예매율 등 핵심 지표 및 실시간 예매 현황을 한눈에 확인합니다. | `AdminHomePanel.java`, `AdminDAO.java` |
| **영화/상영관/스케줄 관리** | 영화 정보, 상영관 규모, 상영 일정을 등록, 수정, 삭제합니다. | `AdminMainPanel.java`, `MovieDAO.java`, `ScreenAdminPanel.java`, `ScheduleAdminPanel.java` |
| **매점 관리** | 매점 메뉴를 추가/수정/삭제하고 품절 상태를 관리합니다. | `SnackAdminPanel.java` |
| **매출 분석** | 월별 총 매출 및 객단가, 영화별 매출 기여도, 주간 매출 추이 등을 분석합니다. | `SalesAnalysisPanel.java`, `AdminDAO.java` |
| **회원 관리** | 일반 사용자 목록을 조회하고, 특정 사용자를 정지(BANNED) 상태로 변경하여 로그인 권한을 제어합니다. | `UserManagementPanel.java`, `UserDAO.java` |

## 🛠️ 기술 스택

* **언어:** Java
* **프레임워크/라이브러리:** Swing (GUI), JDBC
* **데이터베이스:** Oracle (DBUtil.java에 설정 정보 포함)

## 📦 프로젝트 패키지 구조 (Refactored)

| 패키지 | 역할 | 주요 파일 예시 |
| :--- | :--- | :--- |
| `cinemamain` | 메인 애플리케이션 실행 | `CinemaMain.java` |
| `cinemamain.domain` | 데이터 모델/도메인 객체 | `Movie.java`, `User.java`, `Schedule.java`, `Snack.java` |
| `cinemamain.dao` | 데이터 접근 및 DB 로직 | `MovieDAO.java`, `UserDAO.java`, `ReservationDAO.java`, `AdminDAO.java` |
| `cinemamain.ui` | 모든 사용자 인터페이스(Panel, Dialog) | `LoginPanel.java`, `AdminMainPanel.java`, `UserMainPanel.java`, `SeatSelectionDialog.java` |
| `cinemamain.util` | 공통 유틸리티 | `DBUtil.java`, `UIUtils.java` |
