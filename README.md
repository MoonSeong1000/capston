<div>
<h1> 공공데이터와 A* 알고리즘을 이용한 <br> CCTV 안전 길 찾기 어플리케이션
</div>

## 1-1 프로젝트 개요

날이 갈수록 늘어나는 강력 범죄 발생률이 늘어나고 있다. 사회적 약자에 대한 범죄가 큰 사회적 문제로써 떠오르고 있다. <br>이에 대한 여러 보호 방안이 주어지고 있는데 그 중 하나인 안전 귀가에 대한 문제를 해결하기 위해서, 사용자에게 목적지까지의 '빠른' 경로로 제공해주는 기존의 어플리케이션과는 차별화된 
'안전'을 위한 프로그램을 개발하기로 하였다.  

<div>
 <img width="400" src="/readme_image/1.png"></img>
 <img width="400" src="/readme_image/2.png"></img>
</div>
  
## 1-2. 사회적 약자의 안전한 귀가를 위해서?
사회적 약자의 안전한 귀가를 위해서 크게 4가지 기능을 제공한다.<br>
1) 안전 길찾기 <br>
  - 현재 위치를 GPS로 인식하고, CCTV의 위도,경도 값을 받아와서 목적지까지 CCTV가 있는 안전한 길로 안내<br>
2) 근처 CCTV, 지구대 표시 <br>
  - 근처 CCTV위치와 지구대의 위치를 확인하고, 위급한 상황시 지구대로 연락 가능<br>
3) 긴급 메세지<br> 
  - 보호자에게 현재 사용자의 위치를 제공해주고, 위급한 상황시 메시지 전송 가능<br>
4) 버튼 이벤트<br>
  - 알람 기능, 후레시 기능은 어플 내에서 간단한 조작으로 사용할 수 있고, 볼륨버튼을 3번 누르면 자동으로 메세지 전송<br>


## 시스템 구조
### Flow Chart 

<div>
 <img width="1000" src="/readme_image/flowchart.png"></img>
</div>
