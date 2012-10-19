

function drawWADS(pressedCode, canvas, flipAxis) {

   var context = canvas.getContext('2d');
   width = canvas.getWidth();
   height = canvas.getHeight();

   context.fillStyle   = '#00f';
   context.strokeStyle = '#f00';
   context.lineWidth   = 4;

   vertCode = pressedCode;
   horizCode = pressedCode;
   
   leftCode    = 4;
   rightCode   = 6;
   ULcode      = flipAxis ? 7 : 1;
   DLcode      = flipAxis ? 1 : 7;
   URcode      = flipAxis ? 9 : 3;
   DRcode      = flipAxis ? 3 : 9;
   upCode      = flipAxis ? 8 : 2;
   downCode    = flipAxis ? 2 : 8;

   normalFillStyle = '#999';
   normalStrokeStyle = '#AAA';

   pressedFillStyle = '#00f';
   pressedStrokeStyle = '#f00';

   padding = 5;
   shortTriangleDimension = width / 6;
   longTriangleDimension = width / 3;

   switch(pressedCode)
   {
      case 1:
      vertCode = 2;
      horizCode = 4;
      break;
      case 2:
      vertCode = 2;
      horizCode = -1;
      break;
      case 3:
      vertCode = 2;
      horizCode = 6;
      break;
      case 4:
      vertCode = -1;
      horizCode = 4;
      break;
      case 5:
      vertCode = 0;
      horizCode = 0;
      break;
      case 6:
      vertCode = -1;
      horizCode = 6;
      break;
      case 7:
      vertCode = 8;
      horizCode = 4;
      break;
      case 8:
      vertCode = 8;
      horizCode = -1;
      break;
      case 9:
      vertCode = 8;
      horizCode = 6;
      break;
      default:
      vertCode = -1;
      horizCode = -1;
   }

   // draw left arrow
   context.beginPath();

   context.fillStyle = horizCode == leftCode? pressedFillStyle : normalFillStyle;
   context.strokeStyle = horizCode == leftCode? pressedStrokeStyle : normalStrokeStyle;

   leftVertX = padding;
   leftVertY = height / 2;
   context.moveTo(leftVertX, leftVertY);

   topVertX = leftVertX + shortTriangleDimension;
   topVertY = leftVertY - longTriangleDimension / 2;
   context.lineTo(topVertX, topVertY);

   bottomVertX = topVertX;
   bottomVertY = leftVertY + longTriangleDimension / 2;
   context.lineTo(bottomVertX, bottomVertY);
   context.closePath();

   context.fill();
   context.stroke();


   // draw top arrow
   context.beginPath();

   context.fillStyle = vertCode == upCode ? pressedFillStyle : normalFillStyle;
   context.strokeStyle = vertCode === upCode ? pressedStrokeStyle : normalStrokeStyle;

   topVertX = width / 2
   topVertY = padding;
   context.moveTo(topVertX, topVertY);

   leftVertX = topVertX - longTriangleDimension / 2;
   leftVertY = topVertY + shortTriangleDimension;
   context.lineTo(leftVertX, leftVertY);

   rightVertX = leftVertX + longTriangleDimension;
   rightVertY = leftVertY;
   context.lineTo(rightVertX, rightVertY);

   context.closePath();

   context.fill();
   context.stroke();

   // draw right arrow
   context.beginPath();

   context.fillStyle = horizCode == rightCode ? pressedFillStyle : normalFillStyle;
   context.strokeStyle = horizCode === rightCode ? pressedStrokeStyle : normalStrokeStyle;

   rightVertX = width - padding;
   rightVertY = height / 2;
   context.moveTo(rightVertX, rightVertY);

   topVertX = rightVertX - shortTriangleDimension;
   topVertY = rightVertY - longTriangleDimension / 2;
   context.lineTo(topVertX, topVertY);

   bottomVertX = topVertX
   bottomVertY = topVertY + longTriangleDimension
   context.lineTo(bottomVertX, bottomVertY);

   context.closePath();

   context.fill();
   context.stroke();


   // draw bottom arrow
   context.beginPath();

   context.fillStyle = vertCode == downCode ? pressedFillStyle : normalFillStyle;
   context.strokeStyle = vertCode === downCode ? pressedStrokeStyle : normalStrokeStyle;

   bottomVertX = width / 2
   bottomVertY = height - padding;
   context.moveTo(bottomVertX, bottomVertY);

   leftVertX = bottomVertX - longTriangleDimension / 2;
   leftVertY = bottomVertY - shortTriangleDimension;
   context.lineTo(leftVertX, leftVertY);

   rightVertX = leftVertX + longTriangleDimension;
   rightVertY = leftVertY;
   context.lineTo(rightVertX, rightVertY);

   context.closePath();

   context.fill();
   context.stroke();


   // draw middle
   context.beginPath();

   context.fillStyle = vertCode == 0 ? pressedFillStyle : normalFillStyle;
   context.strokeStyle = vertCode === 0 ? pressedStrokeStyle : normalStrokeStyle;

   ctrX = width / 2 + shortTriangleDimension;
   ctrY = height / 2 + shortTriangleDimension;
   context.moveTo(ctrX , ctrY );

   ctrX = width / 2 + shortTriangleDimension;
   ctrY = height / 2 - shortTriangleDimension;
   context.lineTo(ctrX , ctrY );

   ctrX = width / 2 - shortTriangleDimension;
   ctrY = height / 2 - shortTriangleDimension;
   context.lineTo(ctrX , ctrY );

   ctrX = width / 2 - shortTriangleDimension;
   ctrY = height / 2 + shortTriangleDimension;
   context.lineTo(ctrX , ctrY );

   context.closePath();

   context.fill();
   context.stroke();
}