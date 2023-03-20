let removeToast;
const toasts = (comment) => {
  const node = document.getElementById("warning");
  const com = node.children[0];
  if (node.classList.contains('active')) {
    clearTimeout(removeToast), removeToast = setTimeout(() => {
      node.classList.remove('active');
    }, 1000);
  } else {
    removeToast = setTimeout(function () {
      node.classList.remove("active")
    }, 1000)
  }
  node.classList.add("active");
  com.innerText = comment;
}
