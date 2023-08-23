add_executable(lcd_display
        lcd_display.c
        )
pico_enable_stdio_usb(lcd_display 1)
pico_enable_stdio_uart(lcd_display 0)

target_link_libraries(lcd_display pico_stdlib hardware_i2c hardware_adc pico_multicore)

pico_add_extra_outputs(lcd_display)
