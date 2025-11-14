# Angular Business Line Application

This Angular application was built according to the specifications in `src/curs/cr_app.txt`.

## Features

- **Toolbar Navigation**: Contains application icon on the left and navigation sections on the right
- **Business Line Management**: Main section for managing business lines
- **Business Line Edit Page**: Advanced editing interface for business lines with variables and covers
- **Material UI**: Built using Angular Material components for modern, responsive design
- **No Authentication**: Application runs without authentication requirements

## Application Structure

### Components
- `ToolbarComponent`: Main navigation toolbar with application icon and navigation links
- `BusinessLineComponent`: Main page displaying business line data in a table format
- `BusinessLineEditComponent`: Advanced editing interface for business lines

### Services
- `BusinessLineService`: Provides stub data for business lines with CRUD operations
- `BusinessLineEditService`: Provides advanced editing functionality for business lines

### Data Model
Business Line structure:
- `id`: number (unique identifier)
- `code`: string (char(30) - business line code)
- `name`: string (char(200) - business line name)

Business Line Edit structure:
- `mpCode`: string (business line code)
- `mpName`: string (business line name)
- `mpVars`: array of variables with varCode, varType (IN/VAR/FORMULA), varPath
- `mpCovers`: array of covers with coverCode, coverName, risks

## Layout Requirements Met

✅ **Toolbar**: Contains main section names, right-aligned, with left-aligned application icon  
✅ **Business Line Section**: Left-aligned page title  
✅ **Data Section**: Outlined with rounded rectangle, light gray borders  
✅ **Button Section**: Separate outlined section with "Add Business Line" button  
✅ **Table**: 100% width with columns: id, code, name, actions  
✅ **Action Buttons**: Text buttons with icons, right-aligned, 4px spacing  
✅ **Service Integration**: Stub data service providing sample business lines  
✅ **Button Actions**: "Добавить линию" and "Редактирование" buttons now functional  
✅ **Edit Page**: New page at `/lob-edit` for editing business lines  
✅ **Three Segments**: Basic info, variables, and covers sections as specified  
✅ **Save Functionality**: Save button enabled only when changes detected  
✅ **Search Functionality**: Search fields for variables and covers tables  
✅ **Global Styles**: Uses global style library with business-table and action-button classes  

## New Functionality

### Business Line Edit Page (`/lob-edit`)
- **Page Header**: Shows business line name with save button (right-aligned)
- **Basic Info Segment**: mpCode (enabled only for new records) and mpName fields
- **Variables Segment**: 
  - Add variable button
  - Table with varCode, varType (dropdown), varPath columns
  - Search functionality
  - Edit/Delete actions
- **Covers Segment**:
  - Add cover button
  - Table with coverCode, coverName, risks columns
  - Search functionality
  - Edit/Delete actions

### Navigation
- Click "Добавить линию" → Navigate to `/lob-edit` (new record)
- Click "Редактировать" → Navigate to `/lob-edit/:mpCode` (edit existing)
- Save button only enabled when changes detected
- Automatic change tracking for all form fields

## Running the Application

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start development server:
   ```bash
   npm start
   ```

3. Open browser and navigate to `http://localhost:4200`

## Build

To build the application for production:
```bash
npm run build
```

## Technologies Used

- Angular 18
- Angular Material UI
- TypeScript
- SCSS with global style library
- RxJS for reactive programming
- Angular Router for navigation
