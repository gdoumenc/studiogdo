root = exports ? this
 
root.BOHome = "/"

$ ->
  currentEditablePath = document.getElementById('currentInteractivePath')
  slotSelect = document.getElementById('slotSelect')

root.config = (evt) ->
  
  # if came from go button
  evt?.preventDefault()
    
  sessionStorage.setItem('bocall_host', document.getElementById('bocall_host').value)
  sessionStorage.setItem('bocall_war', document.getElementById('bocall_war').value)
  sessionStorage.setItem('bocall_servlet', document.getElementById('bocall_servlet').value)
  
  # reset pathes
  currentEditablePath.innerHTML = '/'
  slotSelect.innerHTML = ''
  
  # set url
  sessionStorage.setItem('bocall_host', window.location.host) if !sessionStorage.getItem('bocall_host')
  bocall_host = sessionStorage.getItem('bocall_host')
  document.getElementById('bocall_host').value = bocall_host

  sessionStorage.setItem('bocall_war', '') if !sessionStorage.getItem('bocall_war')
  bocall_war = sessionStorage.getItem('bocall_war')
  document.getElementById('bocall_war').value = bocall_war

  sessionStorage.setItem('bocall_servlet', '') if (!sessionStorage.getItem('bocall_servlet'))
  bocall_servlet = sessionStorage.getItem('bocall_servlet')
  document.getElementById('bocall_servlet').value = bocall_servlet

  root.BOUrl = "http://#{bocall_host}/#{bocall_war}/#{bocall_servlet}"
  
  # set event listeners
  $("#format_facet_dev").change (evt)->$('#model_facet').val($("#format_facet_dev").val())
    
  # call login
  user = document.getElementById('user').value
  passwd = document.getElementById('passwd').value
  if user == ""
    addSlotOnCurrentInterractivePath()
  else
    boCall = new BOCall
    boCall.error = (infos) ->
      alert infos
    boCall.done = ->
      addSlotOnCurrentInterractivePath()
    boCall.login(user, passwd)

xhrAddPath = (model, sender) ->
  boCall = new BOCall
  boCall.error = (infos) ->
    alert infos
  boCall.done = ->
    response = document.createElement('div')
    response.innerHTML = this.responseText
    commands = response.children[0].children[1]
    singleStencil = false

    # mise a jour des commandes
    document.getElementById('commands').innerHTML = ''
    document.getElementById('commands').appendChild(commands)

    # traitement different selon que ce soit un slot ou un stencil
    type = response.firstChild.getAttribute('data-type')
    if (type == 'slot')
      # mise a jour du path
      shownPath = atob(sender.value.replace('\n', ''))
      sessionStorage.setItem('path', shownPath)
      document.getElementById('pathDisplay').innerHTML = shownPath
      response.firstChild.innerHTML = '/' + response.firstChild.innerHTML;

    else if (type == 'stencil')
      # mise a jour du path
      shownPath = sender.value
      sessionStorage.setItem('path', shownPath)
      document.getElementById('pathDisplay').innerHTML = shownPath

      select = response.querySelector('select')
      # cas où il n'y a qu'une option
      if (select.childElementCount == 2)
        if (select.children[1].innerHTML == '')
          select.parentNode.hidden = true
        select.value = select.children[1].innerHTML
        select.children[0].removeAttribute('selected', 'selected')

        singleStencil = true;
        # /!\ ne pas supprimer la ligne suivante
        select.setAttribute('data-singleStencil', 'singleStencil') #reload DOM hack
      response.firstChild.innerHTML = '(' + response.firstChild.innerHTML + ')';

    # suppression du chemin après le sender
    next = sender.parentNode.nextSibling
    while (next != null)
      nextnext = next.nextSibling
      next.parentNode.removeChild(next)
      next = nextnext

    # ajout de la réponse
    while (response.childNodes.length > 0)
      sender.parentNode.parentNode.appendChild(response.firstChild)

    # hide du loading
    showLoading(false)

    # si il n'y a qu'un stencil, on le selectionne par default
    if (singleStencil)
      addSlotOnCurrentPath()

  boCall.postFacet(null, model, 'dom5')

# ajout du select des clés des stencils
root.addStencilOnCurrentInterractivePath = (evt) ->
  
  if evt?
    select = evt.target
    resetCurrentEditablePath(select)
    resetSlotSelect(select)
 
  path = currentEditablePath.innerHTML

  bocall = new BOCall
  bocall.done = ->
    
    # does nothing if no keys in slot
    cdata = $(bocall.responseText).find("data").html()
    index = cdata.lastIndexOf(']]')
    nb = cdata.substr(11, index - 11)
    if nb == '0'
       $(select.parentNode).after("<span>(nothing in...)</span>")
       return
       
    # does nothing if no keys in slot
    if nb == '1'
      addSlotOnCurrentInterractivePath()
      return
    
    showLoading(false)
    
    select = "
      <select data-path=\"#{path}\" data-type=\"stencil\" onfocusin=\"saveindex(event)\" onfocusout=\"restoreindex(event)\" onchange=\"addSlotOnCurrentInterractivePath(event)\" class=\"key\">
        <option selected=\"selected\">---</option>
        <option data-path=\"#{path}\" data-value=\"$Key\"></option>
      </select>
      "
  
    expandDOM(select)
    
  count_path = path.substr(0, path.lastIndexOf "/") +  "/$Slots(#{select.options[select.selectedIndex].text})/Keys#"
  bocall.postFacet(null, null, count_path)

# ajout du select de slot
root.addSlotOnCurrentInterractivePath = (evt) ->
  
  if evt?
    select = evt.target
    resetCurrentEditablePath(select)
    resetSlotSelect(select)
 
  path = currentEditablePath.innerHTML

  select = "
    <select data-path=\"#{path}\" data-type=\"slot\" onfocusin=\"saveindex(event)\" onfocusout=\"restoreindex(event)\" onchange=\"addStencilOnCurrentInterractivePath(event)\" class=\"slot\">
      <option disabled=\"disabled\" selected=\"selected\">---</option>
      <option data-path=\"#{path}/$Slots\" data-value=\"Name\"></option>
    </select>
    "

  expandDOM(select)

resetCurrentEditablePath = (select) ->
    currentEditablePath.innerHTML = atob(select.dataset.apath)
    if select.dataset.type == "slot"
      currentEditablePath.innerHTML += "/" if !currentEditablePath.innerHTML.endsWith("/")
      currentEditablePath.innerHTML += select.options[select.selectedIndex].text
    else
      key = select.options[select.selectedIndex].text
      currentEditablePath.innerHTML += "(#{key})" if key != "---"

resetSlotSelect = (select) ->
    sibling.parentNode.removeChild(sibling) while sibling = select.parentNode.nextSibling
  
expandDOM = (dom) ->
  bocall = new BOCall
  bocall.done = ->
    showLoading(false)
    select = document.createElement('span')
    slotSelect.appendChild(select)
    select.innerHTML = bocall.responseText

  showLoading(true)
  bocall.appendBOParam("acceptNoStencil", "true")
  bocall.postFacet(null, dom, 'dom5');

showLoading = (show) ->
  document.getElementById('loading').style.visibility = (show) ? 'visible' : 'hidden'

showResponseLoading = (show) ->
  document.getElementById('responseloading').style.visibility = (show) ? 'visible' : 'hidden'

root.initResponseText = ->
  document.getElementById('responseText').innerHTML = ''
  showResponseLoading(true)

root.stencilAction = (event) ->
  event.preventDefault() if event?
  initResponseText()

  # gets path, adding slot path
  target = event.target
  add = target.parentNode.querySelector('input#stencilButtonPath').value
  path = currentEditablePath.innerHTML
  path =  composePath(path, add) if (add != "")
  
  # adds specifique slots
  slots =  if target.parentNode.querySelector('input[name=slots]').checked then '/$Slots' else ''
  commands = if target.parentNode.querySelector('input[name=commands]').checked then '/$Commands' else ''
  path = path + slots + commands

  bocall = new BOCall
  bocall.done = ->
    showResponseLoading(false)
    document.getElementById('responseText').innerText = this.responseText
  if (target.parentNode.querySelector('input[name=attribut]').checked)
    bocall.appendBOParam("a", target.parentNode.querySelector('input[name=attributs]').value)
    
  showLoading(true)
  bocall.getStencils(btoa(path))

root.getAction = (event) ->
  initResponseText()

  bocall = new BOCall
  bocall.done = ->
    showResponseLoading(false)
    document.getElementById('responseText').innerText = this.responseText
    
  showLoading(true)
  path = btoa(currentEditablePath.innerHTML)
  bocall.postProp(path);

root.setAction = (event) ->
  initResponseText()

  bocall = new BOCall
  bocall.done = ->
    showResponseLoading(false)

  showLoading(true)
  path = btoa(currentEditablePath.innerHTML)
  input = event.target.parentNode.querySelector('input[name=v]')
  bocall.setProp(path, input.value)

root.facetAction = (event) ->
  initResponseText()

  bocall = new BOCall
  bocall.done = ->
    src = document.createTextNode(this.responseText)
    document.getElementById('responseText').appendChild(src)
    document.getElementById('responseHTML').innerHTML = this.responseText

    showResponseLoading(false)

  format = $("#format_facet").val()
  model = $("#model_facet").val()
  path = btoa(currentEditablePath.innerHTML)
  bocall.postFacet(path, model, format)

root.applyAction = (event) ->
  initResponseText()

  params = document.getElementById('params').querySelectorAll('input')
  #command = document.getElementById('commands').children[0]

  # recupération de la commande si necessaire
  #if (command.selectedIndex == 0)
  command = document.getElementById('dashboard_apply').querySelector('input[name=c]').value
  #else
  #  command = command.value

  bocall = new BOCall
  bocall.done = ->
    document.getElementById('responseText').innerText = this.responseText
    showResponseLoading(false)

  for param in params
    bocall.appendBOParam(param.name, param.value)

  showLoading(true)
  path = btoa(currentEditablePath.innerHTML)
  bocall.applyCommand(path, command)

# adds a parameter to apply command
root.addParam = (event) ->
  params = document.getElementById('params')
  param = document.createElement('div')
  label = document.createElement('label')
  input = document.createElement('input')
  remove = document.createElement('span')
  paramnum = 'param' + (params.childElementCount + 1)
  input.setAttribute('type', 'text')
  input.setAttribute('name', paramnum)
  label.innerHTML = paramnum
  remove.setAttribute('onclick', 'removeParam(event);')
  remove.innerHTML = '&ominus;'

  param.appendChild(label)
  param.appendChild(input)
  param.appendChild(remove)
  params.appendChild(param)

# removes a parameter of apply command
root.removeParam = (event) ->
  param = event.target.parentNode
  next = param.nextElementSibling

  while (next != null)
    newNumber = next.children[0].innerHTML.split('param')[1] - 1
    next.children[0].innerHTML = 'param' + newNumber
    next.children[1].setAttribute('name', 'param' + newNumber)

    next = next.nextElementSibling

  param.parentNode.removeChild(param)

setUserPath = (path, recursive) ->
  showLoading(true)

  path = path.replace(/\/{2,}/g, '/') # removes all duplicated '/' in path
  #relative path
  if (path.charAt(0) != '/')
    path = sessionStorage.getItem('path') + '/' + path

  explosedPath = new Array()
  slots = path.split('/')

  for slot in slots
    search = slot.match(/(.*)\((.*)\)/)
    stencil = null
    if (search)
      s = search[1]
      stencil = search[2]
    else
      s = (slot == '') ? '/' : slot
    explosedPath.push([ s, stencil ])

  # facet construction
  facet = ''
  tempPath = '/'
  
  for i in [1...explosedPath.length]
    if (recursive || i == (explosedPath.length - 1))
      facet += '<span data-path="' + tempPath + '" data-type="slot">/'
      facet += '<select data-path="'
      facet += tempPath
      facet += '"onfocusin="saveindex(event)" onfocusout="restoreindex(event);" onchange="addStencil(this);">'
      facet += '<option disabled="disabled" selected="selected">---</option>'
      facet += '<option data-path="' + tempPath + '/$Slots" data-value="Pwd" data-label="Name"></option>'
      facet += '</select></span>'
    else
      facet += '<span data-path="' + tempPath + '" data-type="slot">/'
      facet += explosedPath[i][0] + '</span>'
  
    tempPath += '/' + explosedPath[i][0];
    if (explosedPath[i][1])
      if (recursive || i == (explosedPath.length - 1))
        facet += '<span data-path="' + tempPath + '" data-type="stencil">('
        facet += '<select data-path="'
        facet += tempPath
        facet += '"onfocusin="saveindex(event)" onfocusout="restoreindex(event);" onchange="addSlotOnCurrentPath(this);">'
        facet += '<option disabled="disabled" selected="selected">---</option>'
        facet += '<option data-path="' + tempPath + '" data-label="@" data-value="^"></option>'
        facet += '</select>)</span>'
      else
        facet += '<span data-path="' + tempPath + '" data-type="stencil">('
        facet += explosedPath[i][1] + ')</span>'
      tempPath += '(' + explosedPath[i][1] + ')'
  

  console.log('explosedPath: ', explosedPath)
  console.log('facet: ' + facet)

  bocall = new BOCall
  bocall.done = ->
    # suppression de l'ancien chemin
    sender = document.getElementById('root').parentNode
    next = sender.nextSibling
    while (next != null)
      nextnext = next.nextSibling
      next.parentNode.removeChild(next)
      next = nextnext

    sender.parentNode.innerHTML += this.responseText

    sender = document.getElementById('root').parentNode
    next = sender.nextElementSibling
    if (recursive)
      for i in [0...explosedPath]
        options = next.children[0].options
        
        if (next.getAttribute('data-type') == 'slot')
          for j in [0...options]
            if (options[j].innerHTML == explosedPath[i][0])
              next.children[0].selectedIndex = j;
              break
              
          next = next.nextElementSibling
          if (!next)
            break
            
        if (explosedPath[i][1])
          options = next.children[0].options
          for j in [0..options.length]
            if (options[j].innerHTML == explosedPath[i][1])
              next.children[0].selectedIndex = j
              break
          next.children[0].selectedIndex = j
          next = next.nextElementSibling
          if (!next)
            break
        else
          next = next.nextElementSibling
          if (!next)
            break
    else
      selects = sender.parentNode.querySelectorAll('span select')
      if (selects && selects[0])
        options = selects[0].options;
        for option in options
          if (option.parentNode.parentNode.getAttribute('data-type') == 'slot')
            if (option.innerHTML == explosedPath[explosedPath.length - 1][0])
              selects[0].selectedIndex = i
              break
          else
            if (option.innerHTML == explosedPath[explosedPath.length - 1][1])
              selects[0].selectedIndex = i
              break
      if (selects && selects[1])
        options = selects[1].options;
        for option in [options]
          if (option.parentNode.parentNode.getAttribute('data-type') == 'slot')
            if (option.innerHTML == explosedPath[explosedPath.length - 1][0])
              selects[1].selectedIndex = i;
              break;
          else
            if (option.innerHTML == explosedPath[explosedPath.length - 1][1])
              selects[1].selectedIndex = i;
              break;

    document.getElementById('pathEdit').style.display = 'none'
    pathDisplay = document.getElementById('pathDisplay')
    pathDisplay.innerHTML = path
    pathDisplay.style.display = 'block'

    sessionStorage.setItem('path', path)
    showLoading(false)
    
  bocall.postFacet(null, facet, 'dom5')

root.saveindex = (event) ->
  select = event.target

  select.setAttribute('data-tempindex', select.selectedIndex)
  select.selectedIndex = 0

root.restoreindex = (event) ->
  select = event.target

  if (select.selectedIndex == 0)
    select.selectedIndex = select.getAttribute('data-tempindex')
    select.removeAttribute('data-tempindex')


