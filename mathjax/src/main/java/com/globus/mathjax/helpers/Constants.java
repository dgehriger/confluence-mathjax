package com.globus.mathjax.helpers;

// Class used to contain shared static configuration details
abstract class Constants {
    private Constants() {
        // private constructor to prevent instantiation
    }

    // The url to used to serve Mathjax before the user provides one
    static final String DEFAULT_URL = "https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML";
    static final String DEFAULT_SERVER_URL = "http://localhost:43603";
    static final String DEFAULT_INLINE_MATHJAX_START_IDENTIFIER = "(mathjax-inline(";
    static final String DEFAULT_INLINE_MATHJAX_END_IDENTIFIER = ")mathjax-inline)";
    static final String DEFAULT_BLOCK_MATHJAX_START_IDENTIFIER = "(mathjax-block(";
    static final String DEFAULT_BLOCK_MATHJAX_END_IDENTIFIER = ")mathjax-block)";
    static final String DEFAULT_MATHJAX_ASCII_MATH_START_IDENTIFIER = "(mathjax-ascii-math(";
    static final String DEFAULT_MATHJAX_ASCII_MATH_END_IDENTIFIER = ")mathjax-ascii-math)";
}