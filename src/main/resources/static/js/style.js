// 헤더 유저(상세) 버튼 
document.addEventListener("DOMContentLoaded", function () {
  const login = document.querySelector(".user-btn");
  const user_detail = document.querySelector(".user-detail");
  if(login){
    login.addEventListener("click", function (e) {
      if (getComputedStyle(user_detail).display === 'none') {
        console.log(e);
        this.classList.add('active');
        user_detail.style.display = 'block';
        user_detail.animate([
          { opacity: 0, transform: "translate(30px,0)" },
          { opacity: 0.2, transform: "translate(30px,52px)" },
          { opacity: 1, transform: "translate(30px,64px)" },
        ], { duration: 500, iterations: 1, fill: "forwards", easing: "ease-out" });
      } else {
        this.classList.remove('active');
        user_detail.style.display = 'none';
      }
    });
    user_detail.addEventListener("click", function (e) {
      e.stopPropagation();
    });
    user_detail.children[3].addEventListener("click", function (e) {
      if (getComputedStyle(user_detail).display === 'block') {
        login.classList.remove('active');
        user_detail.style.display = 'none';
      }
    });
  }
  
  //헤더 메뉴버튼
  const path = window.location.pathname;
  const arr = path.split('/');
  const last = arr.length;
  const menu = document.querySelector(`.hd-menu li a[href="./${arr[last-1]}"]`);
  if(menu){
    menu.parentNode.classList.add('menu-on');
    menu.parentNode.classList.remove('M-box-up');
  }

  // 리스트 타입
  const viewBtn = document.querySelectorAll('.view-type button');
  viewBtn.forEach((btn) => {
    btn.addEventListener("click", (e) => {
      let btnClass = e.target.classList.value;
      let sibling = e.target.parentNode.children;
      const con = document.querySelector(".list-content");
      const conList = document.querySelectorAll(".list");
      for (let i = 0; i < sibling.length; i++) {
        sibling[i].classList.remove('active');
      }
      if (btnClass.includes('list')) {
        sessionStorage.setItem('viewType', 'list')
        con.classList.remove('active');
        conList.forEach((e) => e.classList.remove('active'));
      } else {
        sessionStorage.setItem('viewType', 'img')
        con.classList.add('active');
        conList.forEach((e) => e.classList.add('active'));
      }
      e.target.classList.add('active');
    })
  })

  //추적대상 추가
  const traceAdd = document.querySelectorAll(".trace-add");
  traceAdd.forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.target.classList.toggle('active');
    })
  })
});