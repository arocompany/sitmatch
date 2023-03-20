const pagination = () => {
  const dom =`
  <nav>
    <ul id="pagination">
      <li class="page-prev"><a href="#" class="btn-off">이전</a></li>
      <li class="page-num"><a href="#" class="btn-off active">1</a></li>
      <li class="page-num"><a href="#" class="btn-off">2</a></li>
      <li class="page-num"><a href="#" class="btn-off">3</a></li>
      <li class="page-num"><a href="#" class="btn-off">4</a></li>
      <li class="page-num"><a href="#" class="btn-off">5</a></li>
      <li class="page-num"><a href="#" class="btn-off">6</a></li>
      <li class="page-num"><a href="#" class="btn-off">7</a></li>
      <li class="page-num"><a href="#" class="btn-off">8</a></li>
      <li class="page-num"><a href="#" class="btn-off">9</a></li>
      <li class="page-num"><a href="#" class="btn-off">10</a></li>
      <li class="page-next"><a href="#" class="btn-off">다음</a></li>
    </ul>
  </nav>
  `;
  return document.write(dom);
}