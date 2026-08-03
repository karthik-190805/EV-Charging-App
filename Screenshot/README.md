# EV Charging App – Figures and Captions

This README provides the figure order, recommended image filenames, and ready-to-use captions for the screenshots and architecture images used in the EV Charging App repository.

> **Recommended location:** Place all image files inside the `Screenshots/` folder and save this file as `Screenshots/README.md`.

---

## Recommended Image Filenames

```text
Screenshots/
├── fig01_system_architecture.jpg
├── fig02_role_selection.jpg
├── fig03_1_user_login.jpg
├── fig03_2_login_success_dashboard.jpg
├── fig04_1_nearest_stations_default.jpg
├── fig04_2_marker_popup_station_info.jpg
├── fig04_3_station_details_window.jpg
├── fig04_4_station_features_popup.jpg
├── fig04_5_share_station_details.jpg
├── fig05_1_route_filtering_screen.jpg
├── fig05_2_route_station_list_a.jpg
├── fig05_2_route_station_list_b.jpg
├── fig05_3_selected_station_details.jpg
├── fig06_1_server_login.jpg
├── fig06_2_server_station_configuration_top.jpg
├── fig06_2_server_station_configuration_bottom.jpg
└── README.md
```

---

## Fig. 1. Three-layer System Architecture

<p align="center">
  <img src="./fig01_system_architecture.jpg" alt="Three-layer System Architecture" width="70%">
</p>

<p align="center"><em>Fig. 1. Three-layer system architecture of the EV charging mobile application, showing the presentation layer, application logic layer, and data layer. The architecture highlights the use of RecyclerView, Google Maps SDK, Material Design components, Haversine distance-based filtering, route corridor filtering, location-based sorting, JSON datasets, Gson parsing, and GPS-based location services.</em></p>

---

## Fig. 2. The App Provides Two Options: User and Server

<p align="center">
  <img src="./fig02_role_selection.jpg" alt="Role Selection Screen" width="40%">
</p>

<p align="center"><em>Fig. 2. Role-selection screen of the EV charging app, where the user can choose between <strong>User</strong> mode and <strong>Server</strong> mode. This allows the application to support both EV users and charging-station administrators.</em></p>

---

## Fig. 3.1. Logging in as User

<p align="center">
  <img src="./fig03_1_user_login.jpg" alt="User Login Screen" width="40%">
</p>

<p align="center"><em>Fig. 3.1. User login screen of the EV charging app. The interface allows the user to enter an email address, phone number, or username along with the password. It also provides options for password recovery and new user registration.</em></p>

---

## Fig. 3.2. Login Successful

<p align="center">
  <img src="./fig03_2_login_success_dashboard.jpg" alt="Login Successful - User Dashboard" width="40%">
</p>

<p align="center"><em>Fig. 3.2. Login successful. After authentication, the user is redirected to the main dashboard of the application, where the map interface, route controls, and list of charging stations are displayed.</em></p>

---

## Fig. 4.1. Nearest Stations Displayed When Start and Destination Are Not Submitted

<p align="center">
  <img src="./fig04_1_nearest_stations_default.jpg" alt="Nearest Stations Without Start and Destination" width="40%">
</p>

<p align="center"><em>Fig. 4.1. When the user does not enter the start location and destination, the application automatically displays the nearest charging stations. The map and station list are presented in order of proximity, enabling quick access to nearby charging options.</em></p>

---

## Fig. 4.2. Marker Pop-up Displays Essential Information

<p align="center">
  <img src="./fig04_2_marker_popup_station_info.jpg" alt="Marker Pop-up Information" width="40%">
</p>

<p align="center"><em>Fig. 4.2. On selecting or hovering over a station marker, a pop-up box appears on the map. The pop-up displays essential information such as station name, port availability, charging type, distance, estimated travel time, price per kWh, and operating hours.</em></p>

---

## Fig. 4.3. Station Details Window

<p align="center">
  <img src="./fig04_3_station_details_window.jpg" alt="Station Details Window" width="40%">
</p>

<p align="center"><em>Fig. 4.3. Clicking a station from the list opens a detailed station-information window. This screen displays charging-port details, pricing, operating hours, geographic location, ratings and reviews, and also provides options to view the route in Google Maps and share the station details.</em></p>

---

## Fig. 4.4. Features Dialog Displays Detailed Station Information

<p align="center">
  <img src="./fig04_4_station_features_popup.jpg" alt="Station Features Dialog" width="40%">
</p>

<p align="center"><em>Fig. 4.4. Clicking the <strong>Features</strong> button opens a detailed pop-up window containing extended station information. This includes station type, number of charging points, public availability, contact details, nearby restaurants, nearby cafes, nearby medical shops, reviews, and other useful notes.</em></p>

---

## Fig. 4.5. Sharing Details of the Station

<p align="center">
  <img src="./fig04_5_share_station_details.jpg" alt="Sharing Station Details" width="40%">
</p>

<p align="center"><em>Fig. 4.5. The application allows the user to share charging-station details through supported mobile applications. The shared message can include the station name, charger type, port availability, pricing, operating hours, and location coordinates.</em></p>

---

## Fig. 5.1. Nearest Stations Filtered Along the Chosen Route

<p align="center">
  <img src="./fig05_1_route_filtering_screen.jpg" alt="Route-based Station Filtering" width="40%">
</p>

<p align="center"><em>Fig. 5.1. After entering the start location and destination, the application filters the charging stations and displays those lying within a 2 km corridor around the selected route. The resulting station list is shown in order of proximity to the user.</em></p>

---

## Fig. 5.2. List of Stations in the Chosen Route

<p align="center">
  <img src="./fig05_2_route_station_list_a.jpg" alt="Route Station List - Part A" width="40%">
  <img src="./fig05_2_route_station_list_b.jpg" alt="Route Station List - Part B" width="40%">
</p>

<p align="center"><em>Fig. 5.2. Example station listings displayed for the chosen route. The application presents route-relevant charging stations with details such as station name, charger-port types, distance, estimated travel time, price per kWh, availability status, and operating hours.</em></p>

---

## Fig. 5.3. Details Window of the Selected Station

<p align="center">
  <img src="./fig05_3_selected_station_details.jpg" alt="Selected Route Station Details" width="40%">
</p>

<p align="center"><em>Fig. 5.3. Details window of a selected charging station from the filtered route list. The screen presents charging-port information, pricing, operating hours, geographic location, ratings, and options to view the route or share the station details.</em></p>

---

## Fig. 6.1. Logging in as a Server

<p align="center">
  <img src="./fig06_1_server_login.jpg" alt="Server Login and Station Management" width="40%">
</p>

<p align="center"><em>Fig. 6.1. Server-side access screen. In this mode, the station manager or administrator can enter and manage station-related technical details such as station type, number of chargers, price per kWh, and other operational information.</em></p>

---

## Fig. 6.2. Selecting the Number of Ports and Setting the Operating Hours of the Station

<p align="center">
  <img src="./fig06_2_server_station_configuration_top.jpg" alt="Server Configuration - Top Section" width="40%">
  <img src="./fig06_2_server_station_configuration_bottom.jpg" alt="Server Configuration - Bottom Section" width="40%">
</p>

<p align="center"><em>Fig. 6.2. Server-side configuration screen used to set station parameters. The administrator can specify the number of charging ports, charging price, operating hours, and station status before saving the station details.</em></p>

---

## Notes

- Save this file as `README.md` inside the `Screenshots/` folder.
- If required, you can also reference these figures from the main repository `README.md` using paths such as:

```markdown
![Fig. 1](Screenshots/fig01_system_architecture.jpg)
```

- If the same dashboard screenshot is used for both **Fig. 3.2** and **Fig. 4.1**, that is acceptable, provided the captions remain as defined above.
