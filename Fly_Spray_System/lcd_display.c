
#include <string.h>
#include <stdio.h>
#include "pico/stdlib.h"
#include "hardware/i2c.h"
#include "pico/binary_info.h"
#include "stdbool.h"
#include "hardware/gpio.h"
#include "hardware/adc.h"
#include "pico/multicore.h"



const int LCD_RETURNHOME = 0x02;
const int LCD_ENTRYMODESET = 0x04;
const int LCD_DISPLAYCONTROL = 0x08;
const int LCD_CURSORSHIFT = 0x10;
const int LCD_FUNCTIONSET = 0x20;
const int LCD_SETCGRAMADDR = 0x40;
const int LCD_SETDDRAMADDR = 0x80;

// flags for display entry mode
const int LCD_ENTRYSHIFTINCREMENT = 0x01;
const int LCD_ENTRYLEFT = 0x02;

// flags for display and cursor control
const int LCD_BLINKON = 0x01;
const int LCD_CURSORON = 0x02;
const int LCD_DISPLAYON = 0x04;

// flags for display and cursor shift
const int LCD_MOVERIGHT = 0x04;
const int LCD_DISPLAYMOVE = 0x08;

// flags for function set
const int LCD_5x10DOTS = 0x04;
const int LCD_2LINE = 0x08;
const int LCD_8BITMODE = 0x10;

// flag for backlight control
const int LCD_BACKLIGHT = 0x08;

const int LCD_ENABLE_BIT = 0x04;

const int LCD_CLEARDISPLAY = 0x01;
static int addr = 0x27;
/* Quick helper function for single byte transfers */
void i2c_write_byte(uint8_t val) {
#ifdef i2c_default
    i2c_write_blocking(i2c_default, addr, &val, 1, false);
#endif
}
void lcd_toggle_enable(uint8_t val) {
    // Toggle enable pin on LCD display
    // We cannot do this too quickly or things don't work
    #define DELAY_US 600
    sleep_us(DELAY_US);
    i2c_write_byte(val | LCD_ENABLE_BIT);
    sleep_us(DELAY_US);
    i2c_write_byte(val & ~LCD_ENABLE_BIT);
    sleep_us(DELAY_US);
}
void set_lcd_message(char *msg[]){
    int i = 0;
    for(i=0;i<sizeof(&msg);++i){
      
        printf(msg[i]);
        printf("\n");

    };
    printf("dont");
    
};
const int SPRAY_DURATION_MS = 3500;
const int ADC_READ_PIN = 26;
void light_led(bool light){
    int led_pin = 25;
    gpio_init(led_pin);
    gpio_set_dir(led_pin,true);
    gpio_put(led_pin,light);
}
void init_spray_gpio(int gpio){
    gpio_init(gpio);
    gpio_set_dir(gpio,true);
}

float voltage_value = 0.0f;


const int SDA_PIN = 4;
const int SCL_PIN = 5;

#define LCD_COMMAND 0
void lcd_send_byte(uint8_t val, int mode) {
    uint8_t high = mode | (val & 0xF0) | LCD_BACKLIGHT;
    uint8_t low = mode | ((val << 4) & 0xF0) | LCD_BACKLIGHT;

    i2c_write_byte(high);
    lcd_toggle_enable(high);
    i2c_write_byte(low);
    lcd_toggle_enable(low);
}
#define LCD_CHARACTER  1
static void inline lcd_char(char val) {
    lcd_send_byte(val, LCD_CHARACTER);
}
void lcd_string(const char *s) {
    while (*s) {
        lcd_char(*s++);
    }
}


void lcd_set_cursor(int line, int position) {
    int val = (line == 0) ? 0x80 + position : 0xC0 + position;
    lcd_send_byte(val, LCD_COMMAND);
}


void lcd_clear(){
    lcd_send_byte(LCD_CLEARDISPLAY, LCD_COMMAND);
}
void lcd_init() {
    lcd_send_byte(0x03, LCD_COMMAND);
    lcd_send_byte(0x03, LCD_COMMAND);
    lcd_send_byte(0x03, LCD_COMMAND);
    lcd_send_byte(0x02, LCD_COMMAND);

    lcd_send_byte(LCD_ENTRYMODESET | LCD_ENTRYLEFT, LCD_COMMAND);
    lcd_send_byte(LCD_FUNCTIONSET | LCD_2LINE, LCD_COMMAND);
    lcd_send_byte(LCD_DISPLAYCONTROL | LCD_DISPLAYON, LCD_COMMAND);
    lcd_clear();
}

const int LINE_MAX_CHARS = 16;
const int READ_TIME_INTERVAL_MS = 2500;
const float SPRAY_INTERVAL_24 = 2.90f;
const float SPRAY_INTERVAL_12 = 2.60f;
const float SPRAY_INTERVAL_6 = 2.2f;
const float SPRAY_INTERVAL_4 = 1.4f;
const float ROUND_MULT = 0.1f;
const int SPRAY_INTERVAL_INIT_COUNTDOWN = 20;
const int SPRAY_INTERVAL_24_COUNTDOWN = 1440*24;
const int SPRAY_INTERVAL_12_COUNTDOWN = 1440*12;
const int SPRAY_INTERVAL_6_COUNTDOWN = 1440*6;
const int SPRAY_INTERVAL_4_COUNTDOWN = 1440*4;
int countdown_start = SPRAY_INTERVAL_24_COUNTDOWN;
int running_countdown = SPRAY_INTERVAL_24_COUNTDOWN;

bool interval_changed = true;
float last_interval = SPRAY_INTERVAL_24;
float current_interval = SPRAY_INTERVAL_24;

const int SPRAY_GPIO = 14;
bool update_running_count(){
    --running_countdown;
    printf("%i\n",running_countdown);
    return running_countdown==0;
}
void set_current_interval(float interval){
    current_interval = interval;
}
void set_initial_countdown(){
    running_countdown = SPRAY_INTERVAL_INIT_COUNTDOWN;
    printf("Initial Countdown: %i\n",running_countdown);
}
bool check_for_changed_interval(){
    if(last_interval!=current_interval){
        printf("Changed Interval\n");
        last_interval = current_interval;
        set_initial_countdown();
        return true;
    }
    return false;
}
void set_countdown_start(int start){
    countdown_start = start;
}
void set_voltage_value(){
    
    light_led(true);
    adc_init();
    adc_gpio_init(ADC_READ_PIN);
    adc_select_input(0);
    const float conv_factor = 3.3f / (1 << 12);

    while(1){
        
        int read_value = adc_read();
        voltage_value = read_value * conv_factor;
        printf("%f",voltage_value);
        printf("\n");
        sleep_ms(2500);
        check_for_changed_interval();
        adc_fifo_drain();
    }
}
char* get_lcd_message(){
    int multiples = voltage_value/ROUND_MULT;

    float voltage_rounded = ROUND_MULT * multiples;
    if(voltage_rounded >= SPRAY_INTERVAL_24){
        set_current_interval(SPRAY_INTERVAL_24);
        set_countdown_start(SPRAY_INTERVAL_24_COUNTDOWN);
        return "1x per day";
    }else if(voltage_rounded < SPRAY_INTERVAL_12 & voltage_rounded > SPRAY_INTERVAL_6){
        set_current_interval(SPRAY_INTERVAL_12);
        set_countdown_start(SPRAY_INTERVAL_12_COUNTDOWN);
        return "2x per day";
    }else if(voltage_rounded < SPRAY_INTERVAL_6 & voltage_rounded > SPRAY_INTERVAL_4){
        set_current_interval(SPRAY_INTERVAL_6);
        set_countdown_start(SPRAY_INTERVAL_6_COUNTDOWN);
        return "4x per day";
    }
    set_current_interval(SPRAY_INTERVAL_4);
    set_countdown_start(SPRAY_INTERVAL_4_COUNTDOWN);
    return "6x per day";

}
void spray(int gpio){
    printf("Spraying");
    gpio_put(gpio,true);
    sleep_ms(SPRAY_DURATION_MS);
    gpio_put(gpio,false);
    running_countdown = countdown_start;
}
void display_voltage(){
       
    
    i2c_init(i2c0, 100 * 1000);
    gpio_set_function(SDA_PIN, GPIO_FUNC_I2C);
    gpio_set_function(SCL_PIN, GPIO_FUNC_I2C);
    gpio_pull_up(SDA_PIN);
    gpio_pull_up(SCL_PIN);
    // Make the I2C pins available to picotool
    bi_decl(bi_2pins_with_func(SDA_PIN, SCL_PIN, GPIO_FUNC_I2C));
    lcd_init();
    printf("sam");
     while(1){
        lcd_set_cursor(0,2);
        lcd_string("Set to Spray");
        lcd_set_cursor(1,3);
        lcd_string(get_lcd_message());
        printf("\nUPDATED THE DISPLAY\n");
        sleep_ms(2500);
        lcd_clear();
        if(update_running_count()){
            spray(SPRAY_GPIO);
        }
    } 
}
int main(){
    stdio_init_all();
    init_spray_gpio(SPRAY_GPIO);
    multicore_launch_core1(set_voltage_value);
    display_voltage();
};