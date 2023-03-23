// 헤더 유저(상세) 버튼 
document.addEventListener("DOMContentLoaded", function () {
  const detailBtn = document.querySelectorAll("header .detail-btn");
  if (detailBtn) {
    for (let i = 0; i < detailBtn.length; i++) {
      detailBtn[i].addEventListener("click", function (e) {
        let detail = this.querySelector('.login-detail');
        if (getComputedStyle(detail).display === 'none') {
          this.classList.add('active');
          detail.style.display = 'unset';
          detail.animate([
            { opacity: 0, transform: "translate(0,0)" },
            { opacity: 0.2, transform: "translate(0,52px)" },
            { opacity: 1, transform: "translate(0,64px)" },
          ], { duration: 500, iterations: 1, fill: "forwards", easing: "ease-out" });
        } else {
          this.classList.remove('active');
          detail.style.display = 'none';
        }
      });
      detailBtn[i].querySelector('.esc').addEventListener("click", function (e) {
        e.stopPropagation();
        if (getComputedStyle(this.parentNode).display === 'block') {
          this.parentNode.parentNode.classList.remove('active');
          this.parentNode.style.display = 'none';
        }
      });
    }
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

  // 헤더 셀렉트 구현
  const select = document.querySelector(".setSearchNumber");
  const option = document.querySelectorAll(".setSearchNumber .option li");
  const tType = document.querySelector('input[name=searchNumber]');
  select.addEventListener("click", (e) => {
    e.stopPropagation();
    select.classList.toggle('active');
    option.forEach(btn => {
      btn.addEventListener('click', (e) => {
        select.children[0].innerHTML = e.target.innerText;
        tType.value = e.target.innerText;
      })
    });
  });

});

