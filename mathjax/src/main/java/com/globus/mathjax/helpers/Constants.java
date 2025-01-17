package com.globus.mathjax.helpers;

// Class used to contain shared static configuration details
abstract class Constants {
    // The url to used to serve Mathjax before the user provides one
    static String DEFAULT_URL = "https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML";
    static String DEFAULT_SERVER_URL = "http://localhost:43603";
    static String DEFAULT_INLINE_MATHJAX_START_IDENTIFIER = "(mathjax-inline(";
    static String DEFAULT_INLINE_MATHJAX_END_IDENTIFIER = ")mathjax-inline)";
    static String DEFAULT_BLOCK_MATHJAX_START_IDENTIFIER = "(mathjax-block(";
    static String DEFAULT_BLOCK_MATHJAX_END_IDENTIFIER = ")mathjax-block)";
    static String DEFAULT_MATHJAX_ASCII_MATH_START_IDENTIFIER = "(mathjax-ascii-math(";
    static String DEFAULT_MATHJAX_ASCII_MATH_END_IDENTIFIER = ")mathjax-ascii-math)";
}