import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  clientLogo:string='';

  constructor(private httpService: HttpService,) { }

  ngOnInit(): void {
    this.getLogoImage();
  }

  /*Rendered the logo image */
  getLogoImage() {
    this.httpService.getUploadedImage().pipe(first()).subscribe((data) => {
      if (data['image']) {
        this.clientLogo = 'data:image/png;base64,' + data['image'];
      } else {
        this.clientLogo = undefined;
      }
    });
  }
}
