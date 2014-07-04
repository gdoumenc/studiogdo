function Popup(title, width, height) {
  var self = this;
  self.overlayElement = null;
  self.modalWindowElement = null;

  var isTouchSupported = 'ontouchstart' in window.document;
  var startEvent = isTouchSupported ? 'touchstart' : 'mousedown';
  var moveEvent = isTouchSupported ? 'touchmove' : 'mousemove';
  var endEvent = isTouchSupported ? 'touchend' : 'mouseup';

  window.addEventListener("resize", handleResize);
  function handleResize() {
    if (self.modalWindowElement) {
      var w = self.modalWindowElement;
      w.style.left = (window.innerWidth - w.offsetWidth) / 2 + "px";
      w.style.top = (window.innerHeight - w.offsetHeight) / 2 + "px";
    }
  }

  self.header = document.createElement("div");
  self.header.className = "modalWindowHeader";
  self.header.innerHTML = "<p>"
      + title
      + "<button id=\"annuler\" class=\"close\" title=\"Fermer\">Fermer</button>"
      + "<button id=\"valider\" class=\"commit\" title=\"Valider\">Valider</button></p>";

  var moveEnabled = false;
  var lastX;
  var lastY;
  self.header.addEventListener(startEvent, startMove);
  window.addEventListener(moveEvent, function() {
    moveEnabled = false;
  });
  self.header.addEventListener(endEvent, handleMove);
  function handleMove(evt) {
    if (self.modalWindowElement && moveEnabled) {
      var deltaX = evt.clientX - lastX;
      var deltaY = evt.clientY - lastY;
      var w = self.modalWindowElement;
      w.style.left = parseInt(w.style.left) + deltaX + "px";
      w.style.top = parseInt(w.style.top) + deltaY + "px";
      lastX = evt.clientX;
      lastY = evt.clientY;
    }
  }
  function startMove(evt) {
    if (evt.target != self.header) {
      return;
    }
    moveEnabled = true;
    lastX = evt.clientX;
    lastY = evt.clientY;
  }

  self.content = document.createElement("div");
  self.content.className = "modalWindowContent";

  self.overlayElement = document.createElement("div");
  self.overlayElement.className = 'modalOverlay';
  self.modalWindowElement = document.createElement("div");
  self.modalWindowElement.className = 'modalWindow hidden';

  if (width) self.modalWindowElement.style.width = width + "px";
  if (height) self.modalWindowElement.style.height = height + "px";
  self.modalWindowElement.appendChild(self.header);
  self.modalWindowElement.appendChild(self.content);
  document.body.appendChild(self.overlayElement);
  document.body.appendChild(self.modalWindowElement);

  setTimeout(function() {
    self.modalWindowElement.style.opacity = 1;
    self.overlayElement.style.opacity = 0.4;
  }, 300);

  this.annuler = function(evt) {
    if (evt) evt.preventDefault();
    alert("to be defined");
  }

  this.valider = function(evt) {
    if (evt) evt.preventDefault();
    alert("to be defined");
  }

  this.show = function() {
    var w = self.modalWindowElement;

    self.modalWindowElement.className = 'modalWindow';
    self.annulerButton = self.header.querySelector("#annuler");
    self.annulerButton.addEventListener('click', self.annuler);
    self.validerButton = self.header.querySelector("#valider");
    self.validerButton.addEventListener('click', self.valider);

    var width = parseInt(w.style.width);
    if (isNaN(width)) width = w.clientWidth;
    var height = parseInt(w.style.height);
    if (isNaN(height)) height = w.clientHeight;
    w.style.left = (window.innerWidth - width) / 2 + "px";
    w.style.top = (window.innerHeight - height) / 2 + "px";
  }

  this.hide = function() {
    self.modalWindowElement.style.opacity = 0;
    self.overlayElement.style.opacity = 0;
    setTimeout(function() {
      document.body.removeChild(self.overlayElement);
      document.body.removeChild(self.modalWindowElement);
      window.removeEventListener("resize", handleResize);
      window.removeEventListener("mousemove", handleMove);
    }, 400);
  }
}
