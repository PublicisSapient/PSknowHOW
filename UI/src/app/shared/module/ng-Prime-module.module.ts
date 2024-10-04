import { NgModule } from "@angular/core";

import { InputSwitchModule } from 'primeng/inputswitch';
import { CarouselModule } from 'primeng/carousel';
import { InputNumberModule } from 'primeng/inputnumber';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { CalendarModule } from 'primeng/calendar';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AccordionModule } from 'primeng/accordion';
import { TooltipModule } from 'primeng/tooltip';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ChipsModule } from 'primeng/chips';
import { RadioButtonModule } from 'primeng/radiobutton';
import { SelectButtonModule } from 'primeng/selectbutton';
import { RippleModule } from 'primeng/ripple';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MenuModule } from 'primeng/menu';
import { CheckboxModule } from 'primeng/checkbox';
import { ScrollTopModule } from 'primeng/scrolltop';
import { BadgeModule } from 'primeng/badge';
import { TabViewModule } from 'primeng/tabview';
import { TableModule } from 'primeng/table';
import { TabMenuModule } from 'primeng/tabmenu';
import { SkeletonModule } from 'primeng/skeleton';
import { DialogService } from 'primeng/dynamicdialog';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { KeyFilterModule } from 'primeng/keyfilter';
import { FieldsetModule } from 'primeng/fieldset';
import { PasswordModule } from 'primeng/password';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { PanelMenuModule } from 'primeng/panelmenu';
import { FileUploadModule } from 'primeng/fileupload';
import { CardModule } from 'primeng/card';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import {StepsModule} from 'primeng/steps';
import { SplitButtonModule } from 'primeng/splitbutton';
import { BlockUIModule } from 'primeng/blockui';
import { PanelModule } from 'primeng/panel';
import { DataViewModule } from 'primeng/dataview';
import { ToolbarModule } from 'primeng/toolbar';
import { FocusTrapModule } from 'primeng/focustrap';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';




@NgModule({
    imports: [
        ToastModule,
        TooltipModule,
        InputTextModule,
        ButtonModule,
        ChipsModule,
        RadioButtonModule,
        InputSwitchModule,
        CarouselModule,
        InputNumberModule,
        OverlayPanelModule,
        DropdownModule,
        DialogModule,
        ToastModule,
        ConfirmDialogModule,
        AccordionModule,
        CalendarModule,
        InputTextModule,
        ScrollTopModule,
        DragDropModule,
        OverlayPanelModule,
        MenuModule,
        CheckboxModule,
        SkeletonModule,
        InputSwitchModule,
        RippleModule,
        BadgeModule,
        TabViewModule,
        TableModule,
        ButtonModule,
        TabMenuModule,
        ToastModule,
        RadioButtonModule,
        InputTextareaModule,
        AccordionModule,
        MultiSelectModule,
        SelectButtonModule,
        DropdownModule,
        KeyFilterModule,
        FieldsetModule,
        PasswordModule,
        AutoCompleteModule,
        PanelMenuModule,
        FileUploadModule,
        CardModule,
        ConfirmPopupModule, 
        StepsModule,
        SplitButtonModule, 
        BlockUIModule, 
        PanelModule, 
        DataViewModule, 
        ToolbarModule, 
        FocusTrapModule, 
        MessagesModule 
    ],
    exports: [
        ToastModule,
        TooltipModule,
        InputTextModule,
        ButtonModule,
        ChipsModule,
        RadioButtonModule,
        InputSwitchModule,
        CarouselModule,
        InputNumberModule,
        OverlayPanelModule,
        DropdownModule,
        DialogModule,
        ToastModule,
        ConfirmDialogModule,
        AccordionModule,
        CalendarModule,
        InputTextModule,
        ScrollTopModule,
        DragDropModule,
        OverlayPanelModule,
        MenuModule,
        CheckboxModule,
        SkeletonModule,
        InputSwitchModule,
        RippleModule,
        BadgeModule,
        TabViewModule,
        TableModule,
        ButtonModule,
        TabMenuModule,
        ToastModule,
        RadioButtonModule,
        InputTextareaModule,
        AccordionModule,
        MultiSelectModule,
        SelectButtonModule,
        DropdownModule,
        KeyFilterModule,
        FieldsetModule,
        PasswordModule,
        AutoCompleteModule,
        PanelMenuModule,
        FileUploadModule,
        CardModule,
        ConfirmPopupModule, 
        StepsModule,
        SplitButtonModule, 
        BlockUIModule, 
        PanelModule, 
        DataViewModule, 
        ToolbarModule, 
        FocusTrapModule, 
        MessagesModule 
    ],
    declarations: [],
    providers: [
        MessageService,
        ConfirmationService,
        DialogService,
    ]
  })
  export class NgPrimeModuleModule { }