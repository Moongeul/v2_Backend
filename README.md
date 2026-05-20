# 😺 뭉글 (moongeul)
> 잊혀지는 문장을 나만의 아카이브로 남기고, 성장으로 이어주는 독서 기록 커뮤니티 앱, 뭉글

<p align="center">
  <img src="https://github.com/user-attachments/assets/9bb09afd-5a7c-4c5d-9caf-27b077ebda5c" width="60%" alt="뭉글 이미지" />
</p>

## 📌 목차 (Table of Contents)
* [📖 소개 (Introduction)](#-소개-introduction)
* [✨ 주요 기능 (Key Features)](#-주요-기능-key-features)
* [🏗 서비스 아키텍처 (Service Architecture)](#-서비스-아키텍처-service-architecture)
* [🛠 개발 환경 (Tech Stack)](#-개발-환경-tech-stack)
* [📜 라이선스 (License)](#-라이선스-license)
 
---

## 📝 소개 (Introduction)
독서를 성장 콘텐츠로 만드는 기록 앱, 뭉글
* **개발 기간**: 2025.09. ~ 2026.03.
* **타겟 사용자**: 트렌디하고 부담 없는 독서 기록 앱을 원하는 2030 '취향 독서가'

## ✨ 주요 기능 (Key Features)
#### 📝 핵심 아카이빙 (Core Archiving)
> * **간단 피드형 기록**: 별점, 감상평, 인용구 입력을 통한 직관적인 독서 기록 인터페이스 제공
> * **'스토리' 공유 기능**: 기록 데이터를 바탕으로 인스타그램 스토리 최적화 비주얼 카드 생성 및 공유

#### 📊 분석 및 커뮤니티 (Analysis & Community)
> * **리포트 & 성장 시각화**: 주간/월간/연간 기록 리포트, 독서 성장 레벨, 가상 책장 시각화 제공
> * **취향 기반 추천 엔진**: 유사 사용자의 감상평 및 인용 포인트를 분석한 개인화 도서 큐레이션
> * **질문 게시판 (Q&A)**: 책에 대한 심도 있는 질문과 답변을 나누는 댓글 기반 소셜 채널

#### ⚙️ 백엔드 서비스 (Backend Services)
> * **도서 검색 (External API)**: Naver Books API를 통한 실시간 도서 정보 검색 및 데이터 캐싱 처리
> * **소셜 로그인 (OAuth2)**: Kakao 및 Google 계정 연동을 통한 간편하고 안전한 인증 시스템
> * **보안 인증 (Security)**: Spring Security와 JWT(JSON Web Token)를 활용한 보안 정책 및 인증 토큰 관리

## 🏗 서비스 아키텍처 (Service Architecture)
<p align="left">
  <img src="https://github.com/user-attachments/assets/bc246fef-ff02-4f1c-97a4-2861ef8615f3" alt="서비스 아키텍처" width="60%" />
</p>

## 🛠 개발 환경 (Tech Stack)

### 💻 Backend & DB

| Category | Skills & Icons | Version / Detail |
| :--- | :--- | :--- |
| **Language** | ![Java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=openjdk&logoColor=white) | Java 17 |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) <br> ![Spring Security](https://img.shields.io/badge/spring%20security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) | Spring Boot 3.5.7, Security, JPA |
| **Database** | ![MariaDB](https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white) | MariaDB |
| **Libraries & API** | ![Kakao](https://img.shields.io/badge/kakao-%23FFCD00.svg?style=for-the-badge&logo=kakao&logoColor=black) <br> ![Google](https://img.shields.io/badge/google-%234285F4.svg?style=for-the-badge&logo=google&logoColor=white) <br> JWT <br> Swagger | OAuth2 (Social Login)<br>Naver Books API (Book Search)<br>JWT (jjwt 0.12.5)<br>Swagger (springdoc-openapi 2.8.9) |

### 🌐 Infrastructure & CI/CD

| Category | Skills & Icons | Version / Detail |
| :--- | :--- | :--- |
| **Server** | ![Oracle Cloud](https://img.shields.io/badge/oracle%20cloud-%23F80000.svg?style=for-the-badge&logo=oracle&logoColor=white) <br> ![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white) | Oracle Cloud, Nginx (Reverse Proxy) |
| **CI/CD** | ![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white) <br> ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) | GitHub Actions · Docker · Docker Hub |
| **IDE** | ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white) | IntelliJ IDEA |

## 📜 라이선스 (License)
Copyright © 2026 Moongle Team. All rights reserved.
이 프로젝트의 모든 권리는 뭉글 팀에 있으며, 무단 전재 및 재배포를 금지합니다.
