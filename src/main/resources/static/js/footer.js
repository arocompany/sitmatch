const footer = () => {
  const dom = `
    
  <footer>
    <div class="warning" id="warning">
      <p class='warn-co'></p>
    </div>
    <div class="modal-bg" id="modal"></div>
    <p class="copy">Copyright ⓒ 서울연구원 All Rights Reserved.</p>
  </footer>
  `;
  return document.write(dom);
}