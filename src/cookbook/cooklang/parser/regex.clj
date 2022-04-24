(ns cookbook.cooklang.parser.regex)

(def comment (re-pattern #"^\s*--\s*(.*)$"))
(def metadata (re-pattern #"^>>\s*(?<key>.+?):\s*(?<value>.+)"))
(def timer (re-pattern #"^~([^#~@\{]+)?\{(?<time>[^\}]*?)(?:%(?<unit>[^}]+)?)?\}"))

(def ingredient
  "Matches ingredients of type:
  - @eggwhite{}
  - @eggwhite{1}
  - @eggwhite{1%g}
  - @egg white{1}
  - @egg"
  (re-pattern #"^@([^#~@\{]+)(?:\{(?<amount>[^\}]+?)?(?:%(?<unit>[^}]+?)?)?\})|@([^\s#~@\{]+)"))

(def cookware
  "Matches cookware of the type:
  - #pan{}
  - #pan{ }
  - #big pan{}
  - #pan"
  (re-pattern #"^#([^#~@\{]+)\{([^\}]*)\}|#([^\s#~@\{]+)"))