// The pattern below is a 'module' pattern based upon iife (immediately invoked function expressions) closures.
// see: http://benalman.com/news/2010/11/immediately-invoked-function-expression/ for a nice discussion of the pattern
// The value of this pattern is to help us keep our variables to ourselves.
let mathjaxHelp = (function ($) {

   // module variables
   let methods = new Object();
   let pluginId = "mathjax";
   let restVersion = "1.0";

   // module methods
   methods['showMathJaxInlineHelp'] = function (e) {
      macroHelpDocumentation.getMacroHelp(e, pluginId, restVersion, "mathjax-inline");
   }
   methods['showMathJaxBlockHelp'] = function (e) {
      macroHelpDocumentation.getMacroHelp(e, pluginId, restVersion, "mathjax-block");
   }

   // return the object with the methods
   return methods;

   // end closure
})(jQuery);
