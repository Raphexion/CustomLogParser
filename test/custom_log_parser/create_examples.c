#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

uint32_t strnlen(const char *s, int maxlen)
{
  uint32_t len = 0;
  while(*s++ != '\0' && len < maxlen)
    len++;

  return len;
}

void write_size(uint32_t size, FILE *fp)
{
  union {
    uint32_t x;
    uint8_t arr[4];
  } x;

  x.arr[0] = (size >> 24) & 0xff;
  x.arr[1] = (size >> 16) & 0xff;
  x.arr[2] = (size >>  8) & 0xff;
  x.arr[3] = (size >>  0) & 0xff;

  fwrite(&x, sizeof(uint32_t), 1, fp);
}

static const char *date1 = "2016-01-01 12:13:14,467";
static const char *xml1 = "  <FOO><BAR>12</BAR></FOO> ";
static const char *fn1 = "test1.log";

int main(int argc, char *argv[])
{
  FILE *fp = fopen(fn1, "wb");
  if (!fp) {
    fprintf(stderr, "Unable to create %s\n", fn1);
    exit(-1);
  }

  const uint32_t datesize1 = strnlen(date1, 128);
  write_size(datesize1, fp);
  fwrite(date1, sizeof(char), datesize1, fp);

  const uint32_t xmlsize1 = strnlen(xml1, 128);
  write_size(xmlsize1, fp);
  fwrite(xml1, sizeof(char), xmlsize1, fp);

  fclose(fp);
}
