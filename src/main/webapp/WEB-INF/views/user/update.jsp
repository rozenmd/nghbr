<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<meta name="viewport" content="width=device-width, initial-scale=1.0">

<html>
<head>
<%-- Stylesheets --%>
<link href="<c:url value="/static/css/bootstrap.min.css" />"
	rel="stylesheet">
<link href="<c:url value="/static/css/font-awesome.min.css" />"
	rel="stylesheet">
<link href="<c:url value="/static/css/bootstrap-social.css" />"
	rel="stylesheet">
<link href="<c:url value="/static/css/landing.css" />" rel="stylesheet">
<link href="<c:url value="/static/css/style.css" />" rel="stylesheet">

	<%--//mapping css--%>
<link rel="stylesheet" href="http://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.css" />



<%-- JavaScripts --%>
<script
	src="<c:url value="/static/js/vendor/modernizr-2.8.3-respond-1.4.2.min.js" />"></script>
<script src="<c:url value="/static/js/vendor/jquery-1.11.2.min.js" />"></script>
<script src="<c:url value="/static/js/vendor/bootstrap.min.js" />"></script>
<%--JS for data display on maps:--%>
	<script src="http://d3js.org/d3.v3.min.js"></script>
	<script src="http://d3js.org/topojson.v1.min.js"></script>
	<script src="http://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.js"></script>
	<script src='//api.tiles.mapbox.com/mapbox.js/plugins/leaflet-omnivore/v0.2.0/leaflet-omnivore.min.js'></script>

<title>NGHBR</title>
</head>

<body>
	<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed"
					data-toggle="collapse" data-target="#navbar" aria-expanded="false"
					aria-controls="navbar">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="/">NGHBR</a>
			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav navbar-right">
					<c:choose>
						<c:when test="${pageContext.request.userPrincipal.authenticated}">
							<li><a href="<c:url value="/messageboard" />">Message board</a></li>
							<li><a href="<c:url value="/profile" />">Profile</a></li>
							<li><a href="<c:url value="/logout" />">Logout</a></li>
						</c:when>
						<c:otherwise>
							<li><a href="<c:url value="/login" />">Login</a></li>
						</c:otherwise>
					</c:choose>
				</ul>

			</div>
			<!--/.navbar-collapse -->
		</div>
	</nav>


	<div  style="padding-top: 40px;padding-bottom:345px;" class="container box well">
		<h3>Profile</h3>
		<div class="table-responsive" style="width:40%;float:left">
			<form method="post" action="update" >

			<table class="table" >
				<c:choose>
					<c:when test="${pageContext.request.userPrincipal.authenticated}">
						<tr>
							<td>First Name</td>
							<td>
								<input type="text" style="width: 185px;"
									   value="${user.firstName}" maxlength="30" name="firstName" id="firstName" />
							</td>
						</tr>
						<tr>
							<td>Last Name</td>
							<td><input type="text" style="width: 185px;"
									   value="${user.lastName}" maxlength="30" name="lastName" id="lastName" /></td>
						</tr>
						<tr>
							<td>Postcode</td>
							<td><input type="text" style="width: 185px;"
									   value="${user.postcode}" maxlength="5" name="postcode" id="postcode" /></td>
						</tr>
						<tr>
							<td>Suburb</td>
							<td><input type="text" style="width: 185px;"
									   value="${user.suburb}" maxlength="30" name="suburb" id="suburb" /></td>
						</tr>
						<tr>
							<td>Email</td>
							<td><input type="text" style="width: 185px;"
									   value="${user.email}" maxlength="30" name="email" id="email" /></td>
						</tr>
						</table>
				<input type="hidden" name="${_csrf.parameterName}"  value="${_csrf.token}" />
			<input type="submit" class="update" title="Update" value="Update" />
			<a href="../profile">Cancel</a>
		</form>

		</c:when>
					<c:otherwise>
						Please <a href="<c:url value="/login" />">login</a> to continue
					</c:otherwise>
				</c:choose>

			<div id="map" style="position: absolute;
    top: 10%;
    left: 44%;
    width: 50%;
    height: 500px;
    float: left;"></div>
		</div>




	</div>
</body>
<script>
	function getColor(d) {
		return d > 1000 ? '#800026' :
				d > 500  ? '#BD0026' :
						d > 200  ? '#E31A1C' :
								d > 100  ? '#FC4E2A' :
										d > 50   ? '#FD8D3C' :
												d > 20   ? '#FEB24C' :
														d > 10   ? '#FED976' :
																'#FFEDA0';
	}

	function style(feature) {
		return {
			// fillColor: getColor(feature.properties.density),
			weight: 1,
			opacity: 1,
			color: 'black',
			dashArray: '3',
			fillOpacity: 0.15
		};
	}
	var mapboxAccessToken = 'pk.eyJ1Ijoicm96ZW5tZCIsImEiOiJlODZmMjk3NDBmYTBhODc5M2U0NDBiYzUyMWM3YjlmOSJ9.muc0sJgn7kvqjzT25Sch-A';
	var tiles = L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=' + mapboxAccessToken, {
				id: 'mapbox.streets',
				attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
				'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
				'Imagery © <a href="http://mapbox.com">Mapbox</a>'
			}),
			latlng = L.latLng(${lat}, ${lon});

	var map = L.map('map', { center: latlng, zoom: 16, layers: [tiles] });
	var customLayer = L.geoJson(null, {style: style});
	L.marker(latlng).addTo(map);
	// this can be any kind of omnivore layer
	var runLayer = omnivore.topojson('/static/js/final.js', null, customLayer).addTo(map);






	var progress = document.getElementById('progress');
	var progressBar = document.getElementById('progress-bar');

	function updateProgressBar(processed, total, elapsed, layersArray) {
		if (elapsed > 1000) {
			// if it takes more than a second to load, display the progress bar:
			progress.style.display = 'block';
			progressBar.style.width = Math.round(processed/total*100) + '%';
		}

		if (processed === total) {
			// all markers processed - hide the progress bar:
			progress.style.display = 'none';
			progressText.style.display = 'none';


		}
	}

</script>
</html>

