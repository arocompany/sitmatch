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
  const searchNumber = document.querySelector(".setSearchNumber");
  searchNumber.addEventListener("click", (e) => {
    e.stopPropagation();
    const option = searchNumber.querySelectorAll(".option li");
    const tType = searchNumber.querySelector('input[name=searchNumber]');
    searchNumber.classList.toggle('active');
    option.forEach(btn => {
      btn.addEventListener('click', (e) => {
        searchNumber.children[0].innerHTML = e.target.innerText;
        tType.value = e.target.innerText;
      })
    });
  });
  // 일치율
  const unity = document.querySelector(".selection.setUnity");
  if (unity) {
    let ops = '';
    for (let i = 100; i >= 5; i -= 5) {
      ops += `<li class='unity_num' value=${i}>${i}</li>`
    }
    unity.querySelector('.option').innerHTML = ops;
  }
  unity.addEventListener("click", (e) => {
    e.stopPropagation();
    const option = unity.querySelectorAll(".option li");
    const tType = unity.querySelector('input[type=hidden]');
    unity.classList.toggle('active');
    option.forEach(btn => {
      btn.addEventListener('click', (e) => {
        unity.children[0].innerHTML = e.target.innerText;
        tType.value = e.target.innerText;
      })
    });
  });

  // 키워드
  const autoKey = document.querySelector(".auto-trace");
  autoKey.addEventListener("click", () => {
    //XMLHttpRequest 객체 생성
    var xhr = new XMLHttpRequest();
    //요청을 보낼 방식, 주소, 비동기여부 설정
    xhr.open('GET', '/keyword', true);
    //요청 전송
    xhr.send(null);
    //통신후 작업
    xhr.onload = () => {
      //통신 성공
      if (xhr.status == 200) {
        document.body.style.overflow = 'hidden';
        modal.style.display = 'flex';
        modal.innerHTML = xhr.response;

        $(".keyword-add-btn").click(function(){
          if((confirm("추가 하시겠습니까?"))){
            var re_list = '';
            $.ajax({
              url: "/add_keyword",
              type: "POST",
              dataType: "json",
              data : {
                keyword : $(".keyword-add").val()
              },
              success: function(data) {
                document.getElementById('newKeyword').value='';
                console.info(data);

                let idxList = [];
                let keywordList = [];
                for(var i=0; i<data.length; i++) {
                  idxList.push(data[i].idx);
                  keywordList.push(data[i].keyword);
                }

                re_list = '<div>';
                for(let i=0; i<data.length; i++){
                  re_list += '<p class="word-list">'
                      +'<input style="background-color: #dfe0e9;" readonly value="'+ data[i].keyword + '"> '
                      +'<button  type="button" class="del-btn btn-off keyword_del"  value="'+data[i].idx+'">' +
                      // +'<button  type="button" onclick="delKeywordBtn2(this.value)" class="del-btn btn-off keyword_del"  value="'+data[i].idx+'">' +
                      '</button></p>';
                  /*re_list += '<p class="word-list">'+data[i].ke+'<span type="button" class="delete keyword_del" value="'+data[i].IDX+'">삭제</span></p>';*/
                }
                re_list += '</div>';
                $(".keyword-box").html(re_list);

                $(".keyword_del").click(function(e){
                  if(confirm("삭제 하시겠습니까?")){

                    let idx = $(this).val();
                    console.log(idx)

                    $(this).parent().remove();

                    $.ajax({
                      url: "/del_keyword",
                      type: "get",
                      data: {
                        idx: idx
                      },
                      dataType: "json",
                      success: function(data) {
                      }
                    });
                  }

                });

              }, error: function (e){
                console.log("error", e)
              }
            });
          }

        });


        $(".keyword_del").click(function(e){
          if((confirm("삭제 하시겠습니까?"))) {
            let idx = $(this).val();
            console.log(idx)
            $(this).parent().remove();

            $.ajax({
              url: "/del_keyword",
              type: "get",
              data: {
                idx: idx
              },
              dataType: "json",
              success: function(data) {
              }
            });
          }
        })

          //  alert("deleteKeyword: "+deleteKeyword+" deleteKeywordValue: "+deleteKeywordValue)
          // $.ajax({
          //   url: "/del_keyword",
          //   type: "get",
          //   data: {
          //     idx: idx
          //   },
          //   dataType: "json",
          //   success: function(data) {
          //
          //     console.info(data);
          //
          //     let idxList = [];
          //     let keywordList = [];
          //     for(var i=0; i<data.length; i++) {
          //       idxList.push(data[i].idx);
          //       keywordList.push(data[i].keyword);
          //     }
          //
          //     /*
          //      re_list += '<p class="word-list">'+ data[i].keyword
          //           +'<span id="deleteKeyword2" type="button" class="delete keyword_del" value="'+data[i].idx+'">' +
          //           '삭제</span></p>';
          //     * */
          //     re_list = '<div>';
          //     setTimeout(function() {
          //       console.log('Works!');
          //     }, 3000);
          //
          //     for(let i=0; i<data.length; i++){
          //       re_list += '<p class="word-list">'
          //           +'<input style="background-color: #dfe0e9;" readonly value="'+ data[i].keyword + '"> '
          //           +'<button  type="button" onclick="delKeywordBtn2(this.value)" class="del-btn btn-off keyword_del" value="'+data[i].idx+'">' +
          //           '</button></p>';
          //       /*re_list += '<p class="word-list">'+data[i].ke+'<span type="button" class="delete keyword_del" value="'+data[i].IDX+'">삭제</span></p>';*/
          //     }
          //     re_list += '</div>';
          //     $(".keyword-box").html(re_list);
          //
          //
          //   },
          //   error: function (e){
          //     console.log(e)
          //   }
          // });
          //
          // })

        // function delKeywordBtn2(idx) {
        //   alert("삭제버튼 클릭1")
        //
        //   $.ajax({
        //     url: "/del_keyword",
        //     type: "get",
        //     data: {
        //       idx: idx
        //     },
        //     dataType: "json",
        //     success: function(data) {
        //
        //       console.info(data);
        //
        //
        //       let idxList = [];
        //       let keywordList = [];
        //       for(var i=0; i<data.length; i++) {
        //         idxList.push(data[i].idx);
        //         keywordList.push(data[i].keyword);
        //       }
        //
        //       /*
        //        re_list += '<p class="word-list">'+ data[i].keyword
        //             +'<span id="deleteKeyword2" type="button" class="delete keyword_del" value="'+data[i].idx+'">' +
        //             '삭제</span></p>';
        //       * */
        //       re_list = '<div>';
        //
        //       for(let i=0; i<data.length; i++){
        //         re_list += '<p class="word-list">'
        //             +'<input style="background-color: #dfe0e9;"  readonly value="'+ data[i].keyword + '"> '
        //             +'<button type="button" onclick="delKeywordBtn(this.value)" class="del-btn btn-off keyword_del"  value="'+data[i].idx+'">' +
        //             '</button></p>';
        //       }
        //       re_list += '</div>';
        //       $(".keyword-box").html(re_list);
        //
        //
        //     },
        //     error: function (e){
        //       console.log(e)
        //     }
        //   });
        //
        // }



        const esc = document.querySelector(".esc-btn");
        esc.onclick = () => {
          modal.style.display = 'none';
          document.body.style.overflow = 'unset';
        }
      } else {
        //통신 실패
        console.log("통신 실패");
      }
    }
  });



});



