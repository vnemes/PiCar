// This application handles over the air updates for the raspberry pi
package main

import (
	"net/http"
	"github.com/labstack/echo"
	"github.com/labstack/echo/middleware"
	"os"
	"io"
	"github.com/labstack/gommon/log"
)

type Message struct {
	Message string `json:"message"`
}

func handleOTA(c echo.Context) error {
	path := c.FormValue("path")
	fileName := c.FormValue("file_name")
	file, err := c.FormFile("file")
	if err != nil {
		return c.JSON(http.StatusBadRequest, &Message{Message: "File is required!"})
	}
	src, err := file.Open()
	if err != nil {
		return c.JSON(http.StatusBadRequest, &Message{Message: "Could not open provided file!"})
	}
	defer src.Close()

	// Destination
	// WARNING: Not very safe but we have auth for this.
	dst, err := os.Create(path + fileName)
	if err != nil {
		return c.JSON(http.StatusBadRequest, &Message{Message: "Could not create destination file." +
			" path or file_name may be invalid!"})
	}
	defer dst.Close()

	// Copy
	if _, err = io.Copy(dst, src); err != nil {
		return err
	}

	return c.JSON(http.StatusOK, &Message{Message: "OTA performed successfully!"})

}

func setupRoutes(e *echo.Echo) {

	e.GET("/", func(c echo.Context) error {
		return c.String(http.StatusOK, "Hello, World!")
	})
	e.POST("/ota", handleOTA)
}

func main() {
	e := echo.New()
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.Logger.SetLevel(log.INFO)

	// Get username and password from environment
	username, ok := os.LookupEnv("OTA_USERNAME");
	if ok != true {
		e.Logger.Warn("Using default username!")
		username = "default"
	}
	password, ok := os.LookupEnv("OTA_PASSWORD");
	if ok != true {
		e.Logger.Warn("Using default password!")
		password = "ota"
	}
	port, ok := os.LookupEnv("OTA_PORT");
	if ok != true {
		port = "1323"
	}

	e.Use(middleware.BasicAuth(func(u, p string, c echo.Context) (bool, error) {
		if username == username && p == password {
			return true, nil
		}
		return false, nil
	}))

	setupRoutes(e)
	e.Logger.Fatal(e.Start(":" + port))
}
