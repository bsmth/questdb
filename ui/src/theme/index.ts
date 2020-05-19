import type { DefaultTheme as DefaultThemeShape } from "styled-components"

import type { ColorShape, FontSizeShape } from "types"

export const color: ColorShape = {
  black: "#191a21",
  gray1: "#585858",
  gray2: "#bbbbbb",
  draculaBackgroundDarker: "#21222c",
  draculaBackground: "#282a36",
  draculaForeground: "#f8f8f2",
  draculaSelection: "#44475a",
  draculaComment: "#6272a4",
  draculaRed: "#ff5555",
  draculaOrange: "#ffb86c",
  draculaYellow: "#f1fa8c",
  draculaGreen: "#50fa7b",
  draculaPurple: "#bd93f9",
  draculaCyan: "#8be9fd",
  draculaPink: "#ff79c6",
  white: "#fafafa",
}

export const fontSize: FontSizeShape = {
  lg: "1.15rem",
  md: "1.4rem",
  sm: "0.75rem",
  xl: "1.125rem",
}

export const theme: DefaultThemeShape = {
  baseFontSize: "16px",
  color,
  font:
    '"Open Sans", -apple-system, BlinkMacSystemFont, Helvetica, Roboto, sans-serif',
  fontMonospace: '"Source Code Pro", monospace',
  fontSize,
}

export type ThemeShape = typeof theme
