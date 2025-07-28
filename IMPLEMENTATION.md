# Event Customization Implementation

## Overview
This implementation adds sorting and filtering capabilities to the DaysLeft Android app, allowing users to customize how their events are displayed.

## Features Implemented

### Sorting Options
- **Date**: Sort events by their date (chronological order) - Default
- **Days Left**: Sort events by number of days remaining until the event
- **Alphabetical**: Sort events alphabetically by title

### Filtering Options  
- **All Events**: Show all events - Default
- **Next 7 Days**: Show only events occurring in the next 7 days
- **This Month**: Show only events occurring within the current month

## Technical Implementation

### Architecture Changes
The implementation follows the existing MVVM architecture pattern with minimal changes:

#### Data Layer
- **EventDao**: Added new query methods for different sorting and filtering combinations
- **EventRepository**: Enhanced interface to accept sort and filter parameters
- **EventRepositoryImpl**: Smart query selection based on filter options

#### Domain Layer
- **SortOption**: Enum defining available sorting methods
- **FilterOption**: Enum defining available filtering methods  
- **GetEvents**: Updated use case to accept sorting/filtering parameters

#### Presentation Layer
- **EventViewModel**: Uses reactive approach with `switchMap` to combine sort/filter selections
- **MainActivity**: Added spinners for sort/filter selection with proper adapters
- **activity_main.xml**: Enhanced layout with sort/filter controls

### Key Technical Decisions

1. **Reactive UI Updates**: Used `switchMap` in ViewModel to ensure immediate UI updates when sort/filter changes
2. **Memory vs Database Sorting**: Date and alphabetical sorting use database queries for efficiency. Days Left sorting uses memory processing since it requires calculation.
3. **Backward Compatibility**: Original `getEvents()` method preserved to maintain existing functionality
4. **Clean Separation**: Each option is handled independently with clear enum definitions

### UI Changes
- Added horizontal layout with two spinners below the "My Events" title
- Left spinner: Sort options
- Right spinner: Filter options  
- Both spinners use Material Design styling with dropdown arrows
- Labels above each spinner indicate their purpose

## Files Modified

### Core Logic
- `EventDao.kt` - Added new query methods
- `EventRepository.kt` - Enhanced interface
- `EventRepositoryImpl.kt` - Smart query implementation
- `EventUseCases.kt` - Updated GetEvents use case
- `EventViewModel.kt` - Added reactive sort/filter state
- `MainActivity.kt` - Wired up spinner controls

### New Files
- `SortOption.kt` - Sort option enum
- `FilterOption.kt` - Filter option enum
- `ic_dropdown_background.xml` - Spinner styling
- `ic_arrow_drop_down.xml` - Dropdown arrow icon

### Enhanced Files
- `DaysLeftUtil.kt` - Added date range calculation methods
- `activity_main.xml` - Added sort/filter UI controls
- `strings.xml` - Added new string resources
- `DaysLeftUtilTest.kt` - Added tests for new utility methods

## Usage
1. Users see two dropdown spinners at the top of the events list
2. Left dropdown allows selecting sort method (Date/Days Left/Alphabetical)
3. Right dropdown allows selecting filter range (All/Next 7 Days/This Month)
4. Changes take effect immediately when selections are made
5. Default behavior preserves existing app functionality (Sort by Date, Show All Events)

## Testing
- Unit tests validate the date range calculations
- Manual testing confirms UI responsiveness
- Backward compatibility maintained for existing functionality