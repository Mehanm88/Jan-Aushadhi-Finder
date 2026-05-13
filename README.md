# Jan-Aushadhi Finder - Clinical Medicine Assistant

Jan-Aushadhi Finder is a professional, clinical-grade Android application designed to promote health literacy and affordable healthcare in India. The app empowers users to find generic equivalents for branded medicines, calculate potential savings, and locate the nearest Jan-Aushadhi Kendras.

## 📱 Project Vision & Social Impact

Healthcare affordability is a critical challenge. Many patients are unaware that **generic medicines** contain the same active ingredients as branded ones but at a fraction of the cost.

- **Promoting Health Literacy**: Through our "About Generics" information system, we educate users on the efficacy and safety of generic salts.
- **Financial Empowerment**: By visualizing potential monthly savings, users can make informed decisions that significantly reduce their healthcare expenditure.
- **Accessibility**: Real-time store tracking and stock check simulations bridge the gap between medicine discovery and procurement.

## 🛠 Technical Implementation

The application follows modern Android development best practices with a focus on performance and robustness.

### Architecture (MVVM + Clean)
- **Room Database**: Local storage for over 500+ medicine entries, including fuzzy search indexing and schema migrations.
- **Repository Pattern**: Abstracted data layer that manages local data (Room) and simulated remote services (Stock checks).
- **Jetpack Compose**: 100% declarative UI with a clinical design system, including custom Shimmer effects and Material 3 components.
- **Coroutines & Flow**: Reactive data streams with search debouncing (300ms) to ensure a flicker-free, high-performance search experience.
- **Google Maps SDK**: Integrated store locator with custom clustering and dark-mode styling.

### Key Technical Features
- **Levenshtein Fuzzy Search**: High typo-tolerance (e.g., 'Para-cetmol' correctly matches 'Paracetamol').
- **Safe Room Migration**: Formal migration strategy (v1 -> v2 -> v3) to preserve user data during schema updates.
- **JSON Backup System**: Export/Import functionality for medication reminders.

## 🚀 Getting Started

### Prerequisites
- **Android Studio Ladybug** (or later)
- **JDK 17**
- **Google Maps API Key**: Required for the 'Store Locator' feature.

### Installation Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Mehanm88/janna.git
   ```
2. **Import Project**: Open Android Studio and select "Open" -> Navigate to the project folder.
3. **API Key Setup**:
   - Create a `local.properties` file in the root directory.
   - Add your key: `MAPS_API_KEY=YOUR_API_KEY_HERE` (or add it directly to `AndroidManifest.xml`).
4. **Build & Run**:
   - Select an Emulator or Physical Device (API 24+).
   - Click the "Run" button (Shift + F10).

## 🧪 Quality Assurance

The app includes a comprehensive test suite:
- **Unit Tests**: `SavingsCalculatorTest` ensures 100% accuracy in financial calculations and edge-case handling.
- **Manual Audit**: Validated search debouncing, location permission rationale, and offline state handling.

---
*Built with ❤️ for a Healthier India.*
