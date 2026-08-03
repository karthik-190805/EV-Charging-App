# EV Charging App – Figures and Captions

This README presents the figures in the correct numerical order, with suitable filenames and professional captions for the EV Charging App repository.

> Place all images inside the `Screenshots/` folder and save this file as `Screenshots/README.md`.

---

## Recommended Image Filenames

```text
Screenshots/
├── fig01_system_architecture.jpg
├── fig02_role_selection.jpg
├── fig03_user_login.jpg
├── fig04_login_success_dashboard.jpg
├── fig05_nearest_stations_default.jpg
├── fig06_marker_popup_station_info.jpg
├── fig07_station_details_window.jpg
├── fig08_station_features_popup.jpg
├── fig09_share_station_details.jpg
├── fig10_route_filtering_screen.jpg
├── fig11_route_station_list_a.jpg
├── fig12_route_station_list_b.jpg
├── fig13_selected_station_details.jpg
├── fig14_server_login.jpg
├── fig15_server_station_configuration_top.jpg
├── fig16_server_station_configuration_bottom.jpg
└── README.md
```

---

## Fig. 1. Three-Layer System Architecture

<p align="center">
  <img src="fig01_system_architecture.jpg" alt="Three-Layer System Architecture" width="70%">
</p>

<p align="center"><em>Fig. 1. Three-layer system architecture of the EV charging mobile application. The presentation layer includes RecyclerView, Google Maps SDK, and Material Design components. The application logic layer performs Haversine distance calculation, 2 km route-corridor filtering, and location-based sorting. The data layer manages the charging-station JSON dataset, Gson parsing, and GPS location services.</em></p>

---

## Fig. 2. User and Server Role Selection

<p align="center">
  <img src="fig02_role_selection.jpg" alt="User and Server Role Selection" width="40%">
</p>

<p align="center"><em>Fig. 2. Role-selection screen of the EV Charging App. The user can choose either User mode for locating charging stations or Server mode for managing charging-station information.</em></p>

---

## Fig. 3. User Login Screen

<p align="center">
  <img src="fig03_user_login.jpg" alt="User Login Screen" width="40%">
</p>

<p align="center"><em>Fig. 3. User login screen of the EV Charging App. The interface accepts an email address, phone number, or username together with a password. It also provides options for password recovery and new user registration.</em></p>

---

## Fig. 4. User Dashboard After Successful Login

<p align="center">
  <img src="fig04_login_success_dashboard.jpg" alt="User Dashboard After Successful Login" width="40%">
</p>

<p align="center"><em>Fig. 4. Main user dashboard displayed after successful login. The screen contains the map interface, charging-station markers, route-input controls, and a list of nearby charging stations.</em></p>

---

## Fig. 5. Nearest Stations Displayed Without Route Input

<p align="center">
  <img src="fig05_nearest_stations_default.jpg" alt="Nearest Stations Without Route Input" width="40%">
</p>

<p align="center"><em>Fig. 5. When the start location and destination are not entered, the application automatically displays the nearest charging stations. The stations are presented on the map and listed in order of proximity.</em></p>

---

## Fig. 6. Charging-Station Marker Information Pop-up

<p align="center">
  <img src="fig06_marker_popup_station_info.jpg" alt="Charging-Station Marker Information Pop-up" width="40%">
</p>

<p align="center"><em>Fig. 6. Selecting a charging-station marker opens an information pop-up. The pop-up displays the station name, port availability, charger type, distance, estimated travel time, price per kWh, and operating hours.</em></p>

---

## Fig. 7. Charging-Station Details Screen

<p align="center">
  <img src="fig07_station_details_window.jpg" alt="Charging-Station Details Screen" width="40%">
</p>

<p align="center"><em>Fig. 7. Detailed information screen for a selected charging station. The screen presents charging-port availability, pricing, operating hours, charger type, geographic coordinates, ratings, and options to view the route, open station features, or share station information.</em></p>

---

## Fig. 8. Detailed Station Features Pop-up

<p align="center">
  <img src="fig08_station_features_popup.jpg" alt="Detailed Station Features Pop-up" width="40%">
</p>

<p align="center"><em>Fig. 8. Station-features pop-up displayed after selecting the Features option. It provides detailed information such as station type, number of charging points, public availability, contact details, nearby restaurants, cafes, medical facilities, reviews, and other useful notes.</em></p>

---

## Fig. 9. Sharing Charging-Station Details

<p align="center">
  <img src="fig09_share_station_details.jpg" alt="Sharing Charging-Station Details" width="40%">
</p>

<p align="center"><em>Fig. 9. Android sharing interface used to share charging-station information through supported applications. The shared content includes station name, charger type, port availability, pricing, operating hours, and geographic coordinates.</em></p>

---

## Fig. 10. Route-Based Charging-Station Filtering

<p align="center">
  <img src="fig10_route_filtering_screen.jpg" alt="Route-Based Charging-Station Filtering" width="40%">
</p>

<p align="center"><em>Fig. 10. After the user enters the start location and destination, the application filters the charging stations and displays those located within a 2 km corridor of the selected route.</em></p>

---

## Fig. 11. First Part of the Route-Based Station List

<p align="center">
  <img src="fig11_route_station_list_a.jpg" alt="First Part of Route-Based Station List" width="40%">
</p>

<p align="center"><em>Fig. 11. First section of the charging-station list generated for the selected route. Each entry displays the station name, charger types, distance, estimated travel time, price per kWh, availability status, and operating hours.</em></p>

---

## Fig. 12. Second Part of the Route-Based Station List

<p align="center">
  <img src="fig12_route_station_list_b.jpg" alt="Second Part of Route-Based Station List" width="40%">
</p>

<p align="center"><em>Fig. 12. Continuation of the charging-station list for the selected route, showing additional nearby stations and their charging-port, pricing, distance, availability, and operating-hour information.</em></p>

---

## Fig. 13. Details of a Selected Route-Based Station

<p align="center">
  <img src="fig13_selected_station_details.jpg" alt="Details of a Selected Route-Based Station" width="40%">
</p>

<p align="center"><em>Fig. 13. Details screen of a charging station selected from the route-filtered list. The screen displays charging-port availability, pricing, operating hours, charger type, geographic coordinates, ratings, and route, feature, and sharing options.</em></p>

---

## Fig. 14. Server-Side Station Management Screen

<p align="center">
  <img src="fig14_server_login.jpg" alt="Server-Side Station Management Screen" width="40%">
</p>

<p align="center"><em>Fig. 14. Server-side station-management screen. The station administrator can enter or update technical and operational information such as station type, number of installed chargers, and charging price.</em></p>

---

## Fig. 15. Server Configuration of Charger Count and Pricing

<p align="center">
  <img src="fig15_server_station_configuration_top.jpg" alt="Server Configuration of Charger Count and Pricing" width="40%">
</p>

<p align="center"><em>Fig. 15. Upper section of the server-side station-configuration screen. The administrator can specify the station type, number of chargers installed, and price per kWh.</em></p>

---

## Fig. 16. Server Configuration of Operating Hours and Status

<p align="center">
  <img src="fig16_server_station_configuration_bottom.jpg" alt="Server Configuration of Operating Hours and Status" width="40%">
</p>

<p align="center"><em>Fig. 16. Lower section of the server-side station-configuration screen. The administrator can set the opening and closing times, update the online status, and save the station details.</em></p>

---

## Notes

- Save this file as `README.md` inside the `Screenshots/` folder.
- Keep all image filenames exactly as listed above.
- The images will appear automatically when the `Screenshots/README.md` file is viewed on GitHub.
- Do not place the image paths inside code blocks.
- Use relative image paths so GitHub displays the images directly.
