; gcd.asm
; assemble with nasm, link with gcc

global  _main
extern  _printf

section .text
_main:
  ; Save registers we use
  push eax 
  push ebx

  mov eax, 84  ; x
  mov ebx, 112 ; y

  ; arguments to printf
  push eax 
  push ebx

gcd:
  cmp eax, ebx
  je  print     ; while (x != y) {
  jle else      ;   if(x > y)
  sub eax, ebx  ;     x = x - y
  jmp gcd       ;
else:           ;   else
  sub ebx, eax  ;     y = y - x
  jmp gcd       ;}

print:
  push  eax ; x and y have already been pushed above
  push  format 
  call  _printf
  add   esp, 16

  ; Restore registers
  pop ebx
  pop eax
  ret

format:
  db  'gcd of %d and %d is %d', 10, 0
