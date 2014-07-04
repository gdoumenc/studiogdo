define ["studiogdo/listAndSelection"], (ListSelection) ->
  
  class ListOrSelection extends ListSelection

    constructor: ->
      super
      
    closeSelectedCallback: (evt) =>
      super
      $(@list).show()
      
    showSelectedBOCallback: =>
      super
      $(@list).hide()
    
    showListBOCallback: (bocall) =>
      super
      $(@list).show()


