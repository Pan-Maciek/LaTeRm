package gui

import scalafx.scene.paint.Color


sealed trait StyleSetter
case class SetForeground(val color: Color) extends StyleSetter
case class SetBackground(val color: Color) extends StyleSetter
case class SetLatex(val on: Boolean) extends StyleSetter
case class SetDefault() extends StyleSetter
case class Skip() extends StyleSetter

case class Style (
  val foreground: Color,
  val background: Color,
  val latexRendering: Boolean,
  val bold: Boolean
) {
  def toggleLatex: Style = copy(latexRendering = !latexRendering)

  def applySgr(sgr: Seq[Int]): Style =
    applySgr(this, sgr)

  @scala.annotation.tailrec
  private def applySgr(style: Style, sgr: Seq[Int]): Style = {
    sgr match {
      case Nil => style

      case fg :: tail if 30 <= fg && fg <= 37 =>
        applySgr(style copy (foreground = Style.foreground4bit(fg)), tail)
      case fg :: tail if 90 <= fg && fg <= 97 =>
        applySgr(style copy (foreground = Style.foreground4bit(fg)), tail)
      case 38 :: 5 :: n :: tail =>
        applySgr(style copy (foreground = Style.foreground8bit(n)), tail)
      case 38 :: 2 :: r :: g :: b :: tail =>
        applySgr(style copy (foreground = Color.rgb(r, g, b)), tail)
      case 39 :: tail =>
        applySgr(style copy (foreground = Style.default.foreground), tail)

      case bg :: tail if 30 <= bg && bg <= 47 =>
        applySgr(style copy (background = Style.background4bit(bg)), tail)
      case bg :: tail if 100 <= bg && bg <= 107 =>
        applySgr(style copy (background = Style.background4bit(bg)), tail)
      case 48 :: 5 :: n :: tail =>
        applySgr(style copy (background = Style.background8bit(n)), tail)
      case 48 :: 2 :: r :: g :: b :: tail =>
        applySgr(style copy (background = Color.rgb(r, g, b)), tail)
      case 49 :: tail =>
        applySgr(style copy (background = Style.default.background), tail)

      case 1 :: tail => applySgr(style.copy(bold = true), tail)
      case 0 :: tail => applySgr(Style.default, tail)
    }
  }
}

object Style {
  val default: Style = Style(
    foreground = Color.White,
    background = Color.Black,
    latexRendering = false,
    bold = false
  )

  // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
  val Sgr4BitColor = Map(
    30 -> Color.rgb(1,1,1), // Black
    31 -> Color.rgb(222,56,43), // Red
    32 -> Color.rgb(57,181,74), // Green
    33 -> Color.rgb(255,199,6), // Yellow
    34 -> Color.rgb(0,111,184), // Blue
    35 -> Color.rgb(118,38,113), // Magenta
    36 -> Color.rgb(44,181,233), // Cyan
    37 -> Color.rgb(204,204,204), // White

    90 -> Color.rgb(128,128,128), // Bright Black
    91 -> Color.rgb(255,0,0), // Bright Red
    92 -> Color.rgb(0,255,0), // Bright Green
    93 -> Color.rgb(255,255,0), // Bright Yellow
    94 -> Color.rgb(0,0,255), // Bright Blue
    95 -> Color.rgb(255,0,255), // Bright Magenta
    96 -> Color.rgb(0,255,255), // Bright Cyan
    97 -> Color.rgb(255,255,255), // Bright White
  )

  val Sgr8BitColor = Map(
    0 -> Color.rgb(0, 0, 0),
    1 -> Color.rgb(128, 0, 0),
    2 -> Color.rgb(0, 128, 0),
    3 -> Color.rgb(128, 128, 0),
    4 -> Color.rgb(0, 0, 128),
    5 -> Color.rgb(128, 0, 128),
    6 -> Color.rgb(0, 128, 128),
    7 -> Color.rgb(192, 192, 192),
    8 -> Color.rgb(128, 128, 128),
    9 -> Color.rgb(255, 0, 0),
    10 -> Color.rgb(0, 255, 0),
    11 -> Color.rgb(255, 255, 0),
    12 -> Color.rgb(0, 0, 255),
    13 -> Color.rgb(255, 0, 255),
    14 -> Color.rgb(0, 255, 255),
    15 -> Color.rgb(255, 255, 255),
    16 -> Color.rgb(0, 0, 0),
    17 -> Color.rgb(0, 0, 95),
    18 -> Color.rgb(0, 0, 135),
    19 -> Color.rgb(0, 0, 175),
    20 -> Color.rgb(0, 0, 215),
    21 -> Color.rgb(0, 0, 255),
    22 -> Color.rgb(0, 95, 0),
    23 -> Color.rgb(0, 95, 95),
    24 -> Color.rgb(0, 95, 135),
    25 -> Color.rgb(0, 95, 175),
    26 -> Color.rgb(0, 95, 215),
    27 -> Color.rgb(0, 95, 255),
    28 -> Color.rgb(0, 135, 0),
    29 -> Color.rgb(0, 135, 95),
    30 -> Color.rgb(0, 135, 135),
    31 -> Color.rgb(0, 135, 175),
    32 -> Color.rgb(0, 135, 215),
    33 -> Color.rgb(0, 135, 255),
    34 -> Color.rgb(0, 175, 0),
    35 -> Color.rgb(0, 175, 95),
    36 -> Color.rgb(0, 175, 135),
    37 -> Color.rgb(0, 175, 175),
    38 -> Color.rgb(0, 175, 215),
    39 -> Color.rgb(0, 175, 255),
    40 -> Color.rgb(0, 215, 0),
    41 -> Color.rgb(0, 215, 95),
    42 -> Color.rgb(0, 215, 135),
    43 -> Color.rgb(0, 215, 175),
    44 -> Color.rgb(0, 215, 215),
    45 -> Color.rgb(0, 215, 255),
    46 -> Color.rgb(0, 255, 0),
    47 -> Color.rgb(0, 255, 95),
    48 -> Color.rgb(0, 255, 135),
    49 -> Color.rgb(0, 255, 175),
    50 -> Color.rgb(0, 255, 215),
    51 -> Color.rgb(0, 255, 255),
    52 -> Color.rgb(95, 0, 0),
    53 -> Color.rgb(95, 0, 95),
    54 -> Color.rgb(95, 0, 135),
    55 -> Color.rgb(95, 0, 175),
    56 -> Color.rgb(95, 0, 215),
    57 -> Color.rgb(95, 0, 255),
    58 -> Color.rgb(95, 95, 0),
    59 -> Color.rgb(95, 95, 95),
    60 -> Color.rgb(95, 95, 135),
    61 -> Color.rgb(95, 95, 175),
    62 -> Color.rgb(95, 95, 215),
    63 -> Color.rgb(95, 95, 255),
    64 -> Color.rgb(95, 135, 0),
    65 -> Color.rgb(95, 135, 95),
    66 -> Color.rgb(95, 135, 135),
    67 -> Color.rgb(95, 135, 175),
    68 -> Color.rgb(95, 135, 215),
    69 -> Color.rgb(95, 135, 255),
    70 -> Color.rgb(95, 175, 0),
    71 -> Color.rgb(95, 175, 95),
    72 -> Color.rgb(95, 175, 135),
    73 -> Color.rgb(95, 175, 175),
    74 -> Color.rgb(95, 175, 215),
    75 -> Color.rgb(95, 175, 255),
    76 -> Color.rgb(95, 215, 0),
    77 -> Color.rgb(95, 215, 95),
    78 -> Color.rgb(95, 215, 135),
    79 -> Color.rgb(95, 215, 175),
    80 -> Color.rgb(95, 215, 215),
    81 -> Color.rgb(95, 215, 255),
    82 -> Color.rgb(95, 255, 0),
    83 -> Color.rgb(95, 255, 95),
    84 -> Color.rgb(95, 255, 135),
    85 -> Color.rgb(95, 255, 175),
    86 -> Color.rgb(95, 255, 215),
    87 -> Color.rgb(95, 255, 255),
    88 -> Color.rgb(135, 0, 0),
    89 -> Color.rgb(135, 0, 95),
    90 -> Color.rgb(135, 0, 135),
    91 -> Color.rgb(135, 0, 175),
    92 -> Color.rgb(135, 0, 215),
    93 -> Color.rgb(135, 0, 255),
    94 -> Color.rgb(135, 95, 0),
    95 -> Color.rgb(135, 95, 95),
    96 -> Color.rgb(135, 95, 135),
    97 -> Color.rgb(135, 95, 175),
    98 -> Color.rgb(135, 95, 215),
    99 -> Color.rgb(135, 95, 255),
    100 -> Color.rgb(135, 135, 0),
    101 -> Color.rgb(135, 135, 95),
    102 -> Color.rgb(135, 135, 135),
    103 -> Color.rgb(135, 135, 175),
    104 -> Color.rgb(135, 135, 215),
    105 -> Color.rgb(135, 135, 255),
    106 -> Color.rgb(135, 175, 0),
    107 -> Color.rgb(135, 175, 95),
    108 -> Color.rgb(135, 175, 135),
    109 -> Color.rgb(135, 175, 175),
    110 -> Color.rgb(135, 175, 215),
    111 -> Color.rgb(135, 175, 255),
    112 -> Color.rgb(135, 215, 0),
    113 -> Color.rgb(135, 215, 95),
    114 -> Color.rgb(135, 215, 135),
    115 -> Color.rgb(135, 215, 175),
    116 -> Color.rgb(135, 215, 215),
    117 -> Color.rgb(135, 215, 255),
    118 -> Color.rgb(135, 255, 0),
    119 -> Color.rgb(135, 255, 95),
    120 -> Color.rgb(135, 255, 135),
    121 -> Color.rgb(135, 255, 175),
    122 -> Color.rgb(135, 255, 215),
    123 -> Color.rgb(135, 255, 255),
    124 -> Color.rgb(175, 0, 0),
    125 -> Color.rgb(175, 0, 95),
    126 -> Color.rgb(175, 0, 135),
    127 -> Color.rgb(175, 0, 175),
    128 -> Color.rgb(175, 0, 215),
    129 -> Color.rgb(175, 0, 255),
    130 -> Color.rgb(175, 95, 0),
    131 -> Color.rgb(175, 95, 95),
    132 -> Color.rgb(175, 95, 135),
    133 -> Color.rgb(175, 95, 175),
    134 -> Color.rgb(175, 95, 215),
    135 -> Color.rgb(175, 95, 255),
    136 -> Color.rgb(175, 135, 0),
    137 -> Color.rgb(175, 135, 95),
    138 -> Color.rgb(175, 135, 135),
    139 -> Color.rgb(175, 135, 175),
    140 -> Color.rgb(175, 135, 215),
    141 -> Color.rgb(175, 135, 255),
    142 -> Color.rgb(175, 175, 0),
    143 -> Color.rgb(175, 175, 95),
    144 -> Color.rgb(175, 175, 135),
    145 -> Color.rgb(175, 175, 175),
    146 -> Color.rgb(175, 175, 215),
    147 -> Color.rgb(175, 175, 255),
    148 -> Color.rgb(175, 215, 0),
    149 -> Color.rgb(175, 215, 95),
    150 -> Color.rgb(175, 215, 135),
    151 -> Color.rgb(175, 215, 175),
    152 -> Color.rgb(175, 215, 215),
    153 -> Color.rgb(175, 215, 255),
    154 -> Color.rgb(175, 255, 0),
    155 -> Color.rgb(175, 255, 95),
    156 -> Color.rgb(175, 255, 135),
    157 -> Color.rgb(175, 255, 175),
    158 -> Color.rgb(175, 255, 215),
    159 -> Color.rgb(175, 255, 255),
    160 -> Color.rgb(215, 0, 0),
    161 -> Color.rgb(215, 0, 95),
    162 -> Color.rgb(215, 0, 135),
    163 -> Color.rgb(215, 0, 175),
    164 -> Color.rgb(215, 0, 215),
    165 -> Color.rgb(215, 0, 255),
    166 -> Color.rgb(215, 95, 0),
    167 -> Color.rgb(215, 95, 95),
    168 -> Color.rgb(215, 95, 135),
    169 -> Color.rgb(215, 95, 175),
    170 -> Color.rgb(215, 95, 215),
    171 -> Color.rgb(215, 95, 255),
    172 -> Color.rgb(215, 135, 0),
    173 -> Color.rgb(215, 135, 95),
    174 -> Color.rgb(215, 135, 135),
    175 -> Color.rgb(215, 135, 175),
    176 -> Color.rgb(215, 135, 215),
    177 -> Color.rgb(215, 135, 255),
    178 -> Color.rgb(215, 175, 0),
    179 -> Color.rgb(215, 175, 95),
    180 -> Color.rgb(215, 175, 135),
    181 -> Color.rgb(215, 175, 175),
    182 -> Color.rgb(215, 175, 215),
    183 -> Color.rgb(215, 175, 255),
    184 -> Color.rgb(215, 215, 0),
    185 -> Color.rgb(215, 215, 95),
    186 -> Color.rgb(215, 215, 135),
    187 -> Color.rgb(215, 215, 175),
    188 -> Color.rgb(215, 215, 215),
    189 -> Color.rgb(215, 215, 255),
    190 -> Color.rgb(215, 255, 0),
    191 -> Color.rgb(215, 255, 95),
    192 -> Color.rgb(215, 255, 135),
    193 -> Color.rgb(215, 255, 175),
    194 -> Color.rgb(215, 255, 215),
    195 -> Color.rgb(215, 255, 255),
    196 -> Color.rgb(255, 0, 0),
    197 -> Color.rgb(255, 0, 95),
    198 -> Color.rgb(255, 0, 135),
    199 -> Color.rgb(255, 0, 175),
    200 -> Color.rgb(255, 0, 215),
    201 -> Color.rgb(255, 0, 255),
    202 -> Color.rgb(255, 95, 0),
    203 -> Color.rgb(255, 95, 95),
    204 -> Color.rgb(255, 95, 135),
    205 -> Color.rgb(255, 95, 175),
    206 -> Color.rgb(255, 95, 215),
    207 -> Color.rgb(255, 95, 255),
    208 -> Color.rgb(255, 135, 0),
    209 -> Color.rgb(255, 135, 95),
    210 -> Color.rgb(255, 135, 135),
    211 -> Color.rgb(255, 135, 175),
    212 -> Color.rgb(255, 135, 215),
    213 -> Color.rgb(255, 135, 255),
    214 -> Color.rgb(255, 175, 0),
    215 -> Color.rgb(255, 175, 95),
    216 -> Color.rgb(255, 175, 135),
    217 -> Color.rgb(255, 175, 175),
    218 -> Color.rgb(255, 175, 215),
    219 -> Color.rgb(255, 175, 255),
    220 -> Color.rgb(255, 215, 0),
    221 -> Color.rgb(255, 215, 95),
    222 -> Color.rgb(255, 215, 135),
    223 -> Color.rgb(255, 215, 175),
    224 -> Color.rgb(255, 215, 215),
    225 -> Color.rgb(255, 215, 255),
    226 -> Color.rgb(255, 255, 0),
    227 -> Color.rgb(255, 255, 95),
    228 -> Color.rgb(255, 255, 135),
    229 -> Color.rgb(255, 255, 175),
    230 -> Color.rgb(255, 255, 215),
    231 -> Color.rgb(255, 255, 255),
    232 -> Color.rgb(8, 8, 8),
    233 -> Color.rgb(18, 18, 18),
    234 -> Color.rgb(28, 28, 28),
    235 -> Color.rgb(38, 38, 38),
    236 -> Color.rgb(48, 48, 48),
    237 -> Color.rgb(58, 58, 58),
    238 -> Color.rgb(68, 68, 68),
    239 -> Color.rgb(78, 78, 78),
    240 -> Color.rgb(88, 88, 88),
    241 -> Color.rgb(98, 98, 98),
    242 -> Color.rgb(108, 108, 108),
    243 -> Color.rgb(118, 118, 118),
    244 -> Color.rgb(128, 128, 128),
    245 -> Color.rgb(138, 138, 138),
    246 -> Color.rgb(148, 148, 148),
    247 -> Color.rgb(158, 158, 158),
    248 -> Color.rgb(168, 168, 168),
    249 -> Color.rgb(178, 178, 178),
    250 -> Color.rgb(188, 188, 188),
    251 -> Color.rgb(198, 198, 198),
    252 -> Color.rgb(208, 208, 208),
    253 -> Color.rgb(218, 218, 218),
    254 -> Color.rgb(228, 228, 228),
    255 -> Color.rgb(238, 238, 238)
  )

  def foreground4bit(fg: Int): Color = Sgr4BitColor(fg)
  def background4bit(bg: Int): Color = Sgr4BitColor(bg - 10)

  def foreground8bit(fg: Int): Color = Sgr8BitColor(fg)
  def background8bit(bg: Int): Color = Sgr8BitColor(bg)
}
