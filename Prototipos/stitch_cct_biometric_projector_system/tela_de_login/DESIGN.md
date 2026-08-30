---
name: Institutional Excellence
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#43474f'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737780'
  outline-variant: '#c3c6d1'
  surface-tint: '#3a5f94'
  primary: '#001e40'
  on-primary: '#ffffff'
  primary-container: '#003366'
  on-primary-container: '#799dd6'
  inverse-primary: '#a7c8ff'
  secondary: '#775a19'
  on-secondary: '#ffffff'
  secondary-container: '#fed488'
  on-secondary-container: '#785a1a'
  tertiary: '#00231f'
  on-tertiary: '#ffffff'
  tertiary-container: '#003a35'
  on-tertiary-container: '#38ac9f'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d5e3ff'
  primary-fixed-dim: '#a7c8ff'
  on-primary-fixed: '#001b3c'
  on-primary-fixed-variant: '#1f477b'
  secondary-fixed: '#ffdea5'
  secondary-fixed-dim: '#e9c176'
  on-secondary-fixed: '#261900'
  on-secondary-fixed-variant: '#5d4201'
  tertiary-fixed: '#89f5e7'
  tertiary-fixed-dim: '#6bd8cb'
  on-tertiary-fixed: '#00201d'
  on-tertiary-fixed-variant: '#005049'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.05em
  data-mono:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 8px
  container-max: 1440px
  gutter: 24px
  margin-desktop: 40px
  margin-tablet: 24px
  margin-mobile: 16px
---

## Brand & Style

The design system is engineered for high-density administrative environments within a prestigious academic context. It prioritizes **Institutional Modernism**—a blend of Corporate Reliability and functional Minimalism. The target audience includes university registrars, department heads, and IT administrators who require high data throughput without cognitive fatigue.

The emotional response should be one of stability, authority, and precision. By utilizing expansive whitespace and a structured typographic hierarchy, the system transforms complex academic data into actionable insights. The visual style avoids unnecessary ornamentation, focusing instead on clarity of information and the speed of task completion.

## Colors

This design system utilizes a high-contrast palette to establish a clear hierarchy of authority. 

- **Primary Blue (#003366):** Represents the institution's heritage and stability. It is used for primary navigation, headers, and main action buttons.
- **Secondary Gold (#C5A059):** Reserved for academic highlights and premium interactive states, providing a sophisticated contrast to the deep blue.
- **Tertiary Teal (#0D9488):** A modern accent used for secondary actions and data visualizations.
- **Surface Palette:** The background is a crisp White (#FFFFFF), with a secondary surface of Light Gray (#F8FAFC) used to differentiate data containers and sidebars.

Status indicators are semantic and fixed: Emerald for availability, Blue for active rentals, Amber for maintenance, and Red for unauthorized access.

## Typography

The design system relies exclusively on **Inter** to ensure maximum legibility across diverse data densities. The font-weight strategy is deliberate: heavy weights (600-700) are used for institutional titles, while medium weights (500) are reserved for data points within tables to ensure readability against various backgrounds.

For data-heavy administrative tasks, use the `data-mono` variant which enables tabular figures (tnum) to ensure columns of numbers align perfectly. Labels use a slight letter-spacing increase and uppercase transformation to differentiate from body text in dense forms.

## Layout & Spacing

The design system employs a **Fixed Grid** philosophy for desktop layouts, centering content within a 1440px container to prevent eye strain on ultra-wide monitors. A strict 8px spacing scale governs all padding and margins.

- **Desktop (12 columns):** 40px margins, 24px gutters. Dashboard cards typically span 3, 4, or 6 columns.
- **Tablet (8 columns):** 24px margins, 16px gutters. Sidebars collapse into icons or a hamburger menu.
- **Mobile (4 columns):** 16px margins, 12px gutters. Metric cards stack vertically.

Content should follow a "Z-pattern" for dashboards, placing critical biometric scan triggers in the top-right header area for immediate access.

## Elevation & Depth

To maintain a professional and "flat" aesthetic that emphasizes content, depth is conveyed through **Tonal Layering** and **Low-contrast Outlines** rather than heavy shadows.

1.  **Level 0 (Base):** Light Gray (#F8FAFC) background.
2.  **Level 1 (Cards/Containers):** Pure White (#FFFFFF) surfaces with a 1px solid border (#E2E8F0).
3.  **Level 2 (Dropdowns/Modals):** Pure White with a very soft, subtle ambient shadow (0px 4px 12px rgba(0, 0, 0, 0.05)) to suggest interaction priority.

This approach creates a "paper-on-table" feel that is appropriate for an academic, administrative environment.

## Shapes

The design system utilizes a **Soft (0.25rem)** shape language. This subtle rounding softens the clinical feel of administrative software without sacrificing the serious, professional nature of a university system.

- **Buttons & Inputs:** 0.25rem (4px) corner radius.
- **Metric Cards:** 0.5rem (8px) corner radius for a more prominent container feel.
- **Status Badges:** Fully rounded (pill) to clearly distinguish them from interactive buttons or input fields.

## Components

### Data Tables
Tables are the core of this design system. Use a "Zebra-stripe" pattern on hover only. Headers must be sticky, utilizing the Primary Blue for text and a bottom border of 2px for clear separation.

### Status Badges
Badges use a "soft-tint" background (10% opacity of the status color) with high-contrast text of the same hue. For example, "Available" uses a light green background with dark green text.

### Metric Cards
Dashboard cards should feature a prominent "Hero Value" in `headline-xl`, a small descriptive label in `label-md`, and a bottom-aligned trend indicator or secondary metric.

### Scan Triggers
The biometric scan trigger is a specialized component. It should be styled as a High-Contrast button (Primary Blue background) with a leading icon (e.g., fingerprint or QR code). When active, it pulses with a subtle Gold (#C5A059) glow.

### Input Fields
Fields use a 1px border (#CBD5E1) that transitions to Primary Blue (#003366) on focus. Error states use the "unauthorized" red for both the border and a small helper text below the field.