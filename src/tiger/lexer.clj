(ns tiger.lexer)

(def token-map
  {
   "+" :TOKEN_ADD,
   "&&" :TOKEN_AND,
   "=" :TOKEN_ASSIGN,
   "boolean" :TOKEN_BOOLEAN,
   "class" :TOKEN_CLASS,
   "," :TOKEN_COMMER,
   "." :TOKEN_DOT,
   "else" :TOKEN_ELSE,
   "EOF" :TOKEN_EOF,
   "extends" :TOKEN_EXTENDS,
   "false" :TOKEN_FALSE,
   "if" :TOKEN_IF,
   "int" :TOKEN_INT,
   "{" :TOKEN_LBRACE,
   "[" :TOKEN_LBRACK,
   "length" :TOKEN_LENGTH,
   "(" :TOKEN_LPAREN,
   "<" :TOKEN_LT,
   "main" :TOKEN_MAIN,
   "new" :TOKEN_NEW,
   "!" :TOKEN_NOT,
   ;NUM
   "out" :TOKEN_OUT,
   "println" :TOKEN_PRINTLN,
   "public" :TOKEN_PUBLIC,
   "}" :TOKEN_RBRACE,
   "]" :TOKEN_RBRACK,
   "return" :TOKEN_RETURN,
   ")" :TOKEN_PAREN,
   ";" :TOKEN_SEMI,
   "static" :TOKEN_STATIC,
   "String" :TOKEN_STRING,
   "-" :TOKEN_SUB,
   "System" :TOKEN_SYSTEM,
   "this" :TOKEN_THIS,
   "*" :TOKEN_TIME,
   "true" :TOKEN_TRUE,
   "void" :TOKEN_VOID,
   "while" :TOKEN_WHILE
   })

(def terminal-set 
  #{\space, \newline, \return, \tab, \+, \=, \,, \., \{, \[, \(, \<,
    \!, \}, \], \), \;, \-, \*})

(defn is-terminal? [c]
  (if (= c -1)
    true
    (some? (terminal-set (char c)))))

(defn is-digit? [n]
  "Test if a Character is digit [0-9]"
  (re-matches #"\d" n))

(defn expect-id-or-digit [s]
  (cond
    (every? is-digit? (map str (apply vector s))) {:kind :TOKEN_NUM, :linenum 0, :lexem s}
    :else {:kind :TOKEN_ID, :linenum 0, :lexem s}))

(defn expect-id-key [fstream terminal s]
  "description"
  (cond
    (= terminal -1) {:kind :TOKEN_EOF, :linenum 0, :lexem "none"}
    (some? (token-map s)) (do
                           (.reset fstream)
                           {:kind (token-map s), :linenum 0, :lexem "none"})
    (and (= s "") (some? (token-map (str (char terminal))))) {:kind (token-map (str (char terminal))), :linenum 0, :lexem "none"}
    :else (expect-id-or-digit s)))

(defn skip-space [fstream]
  (loop [c (.read fstream)]
    (if (or (= c 9)  ; \tab
            (= c 10) ; \newline
            (= c 13) ; \return
            (= c 32)) ; \space)
      (recur (do (.mark fstream 1) (.read fstream)))
      c)))

(defn next-token-internal [fstream pos line]
  (loop [c (skip-space fstream) s ""]
    (cond
      (is-terminal? c) (expect-id-key fstream c s)
      :else (recur (do (.mark fstream 1) (.read fstream)) 
                   (str s (char c))))))

(defn next-token [fstream pos line]
  (do (.mark fstream 1)
    (next-token-internal fstream 0 0)))