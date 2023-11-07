// 리스트 타입
let viewType = sessionStorage.getItem('viewType') ? sessionStorage.getItem('viewType') : 'list' ;
console.log(viewType);
const view_type = () => {
  let view = document.querySelector(`.view-type .${viewType}-btn`);
  view.classList.add('active');
  if(viewType==='img'){
    const con=document.querySelector(".list-content");
    const conList=document.querySelectorAll(".list");
    con.classList.add('active');
    conList.forEach((e)=>e.classList.add('active'));
  }
}

// 자식노드 모드선택
const getSiblings = elm => [...elm.parentNode.children].filter(node => node != elm);

// // 임시 admin/일반 로그인
// const login = () => {
//   if (loginId.value.trim() === 'admin') {
//     sessionStorage.setItem('isAdmin', true);
//     sessionStorage.setItem('isLogin', true);
//   } else {
//     sessionStorage.setItem('isLogin', true);
//   }
//   // location.href = './password';
// }
//
// 로그아웃
// const logout = () => {
//   sessionStorage.removeItem('isLogin');
//   sessionStorage.removeItem('isAdmin');
//   sessionStorage.removeItem('viewType');
//   sessionStorage.removeItem('tabType');
//   // location.href = '/user/login';
// };

