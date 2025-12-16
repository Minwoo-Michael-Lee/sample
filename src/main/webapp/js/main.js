// 메인 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('나이스 학부모서비스 메인 페이지 로드 완료');
    
    // 메뉴 초기화
    initMenu();
    
    // 서비스 카드 클릭 이벤트
    initServiceCards();
    
    // 게시판 아이템 클릭 이벤트
    initBoardItems();
    
    // 호버로 동작하므로 외부 클릭 이벤트 불필요
    
    // 로그아웃 함수
    window.logout = function() {
        if (confirm('로그아웃 하시겠습니까?')) {
            // TODO: 실제 로그아웃 API 호출
            // fetch('/api/auth/logout', { method: 'POST' })
            // .then(() => {
            //     window.location.href = '/login';
            // });
            window.location.href = 'login.html';
        }
    };
});

// 메뉴 초기화 (호버로 동작하므로 별도 이벤트 불필요)
function initMenu() {
    // 호버로 동작하므로 별도 초기화 불필요
}

// 서비스 카드 초기화
function initServiceCards() {
    const serviceCards = document.querySelectorAll('.service-card');
    serviceCards.forEach(card => {
        card.addEventListener('click', function() {
            const serviceName = this.querySelector('.service-card-text').textContent.trim();
            console.log('서비스 선택:', serviceName);
            
            // 각 서비스에 맞는 페이지로 이동
            const serviceMap = {
                '학교\n생활기록': '/student/record',
                '건강\n기록': '/student/health',
                '교육활동\n신청': '/student/activity',
                '늘봄·방과후학교\n신청': '/student/after-school',
                '자녀\n학교정보': '/school/info',
                '학부모\n소통': '/parent/communication'
            };
            
            const path = serviceMap[serviceName] || '/';
            if (path !== '/') {
                window.location.href = path;
            }
        });
    });
}

// 호버로 동작하므로 toggleMenu 함수 불필요

// 게시판 아이템 초기화
function initBoardItems() {
    const boardItems = document.querySelectorAll('.board-item-text');
    boardItems.forEach(item => {
        item.addEventListener('click', function() {
            const text = this.textContent.trim();
            console.log('게시판 아이템 클릭:', text);
            
            // 상세 페이지로 이동하거나 모달 표시 (추후 구현)
            // window.location.href = '/board/detail?id=' + itemId;
        });
    });
}

// 전역 함수 - 탭 전환
function switchTab(tab) {
    const navTabs = document.querySelectorAll('.nav-tab');
    navTabs.forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');
    
    console.log('탭 전환:', tab);
}

// 전역 함수 - 서비스 탭 전환
function switchServiceTab(tab) {
    const serviceTabs = document.querySelectorAll('.service-tab');
    serviceTabs.forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');
    
    updateServiceCards(tab);
}
