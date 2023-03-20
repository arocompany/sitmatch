const isLogin = sessionStorage.getItem('isLogin') ?? null;
const isAdmin = sessionStorage.getItem('isAdmin') ?? null;
 // 익스 접속시 엣지로 자동 열기
 if (window.navigator.userAgent.match(/MSIE|Internet Explorer|Trident/i)) {
  window.location = "microsoft-edge:" + window.location.href;
}
if (!isLogin) location.href = '/user/login';
document.write('<script src="/js/function.js"></script>');
const header = () => {
  // 로그인
  const dom = `
  <header>
  <nav>
    <ul class="hd-menu">
      <li class="M-box-up"><a href="/">Home</a></li>
      <li class="M-box-up"><a href="/history">이력관리</a></li>
      <li class="M-box-up"><a href="/trace">모니터링</a></li>
      <li class="M-box-up"><a href="/result">검색결과</a></li>
      ${isAdmin ? '<li class="M-box-up"><a href="/manage">상담사 관리</a></li>' : ''}
    </ul>
    <ul class="login">
      <li class="M-box-up bor10 login-btn notice-btn"><a href="./notice">알람</a></li>
      <li class="M-box-up bor10 login-btn user-btn">사용자
        <div class="user-detail bor10" >
          <p class="user-name"><strong th:text="${session}">홍길동 님</strong>(usertest0001)</p>
          <a href="/info" class="my-info">내정보</a>
          <button class="logout" onclick='logout();'>로그아웃</button>
          <button class="esc">닫기</button>
        </div>
      </li>
    </ul>
  </nav>
</header>
  `;
  return document.write(dom);
}